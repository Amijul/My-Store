import * as functions from "firebase-functions/v1";
import * as admin from "firebase-admin";

admin.initializeApp();

// Existing: create /users/{uid} on signup
export const onAuthUserCreate = functions.auth.user().onCreate(
  async (user: admin.auth.UserRecord) => {
    await admin.firestore().collection("users").doc(user.uid).set(
      {role: "buyer", status: "active"},
      {merge: true}
    );
    return null;
  }
);

// New: callable function for buyer to create an order
export const createOrder = functions.https.onCall(async (data, context) => {
  // 1) Auth required
  if (!context.auth) {
    throw new functions.https.HttpsError("unauthenticated", "Login required.");
  }

  const buyerId = context.auth.uid;

  // 2) Validate request shape (minimal)
  const sellerId = String(data?.sellerId ?? "").trim();
  const items = data?.items;

  if (!sellerId) {
    throw new functions.https.HttpsError("invalid-argument", "sellerId is required.");
  }

  if (!Array.isArray(items) || items.length === 0) {
    throw new functions.https.HttpsError("invalid-argument", "items must be a non-empty array.");
  }

  // 3) Normalize + validate items (minimal safe version)
  // NOTE: For real commerce, prices should come from your server-side catalog.
  // Here we compute totals from client-provided unitPrice as a placeholder.
  let totalAmount = 0;

  const normalizedItems = items.map((it: any) => {
    const productId = String(it?.productId ?? "").trim();
    const qty = Number(it?.qty ?? 0);
    const unitPrice = Number(it?.unitPrice ?? 0);

    if (!productId) throw new functions.https.HttpsError("invalid-argument", "productId required.");
    if (!Number.isFinite(qty) || qty <= 0) throw new functions.https.HttpsError("invalid-argument", "qty must be > 0.");
    if (!Number.isFinite(unitPrice) || unitPrice < 0) throw new functions.https.HttpsError("invalid-argument", "unitPrice must be >= 0.");

    const lineTotal = qty * unitPrice;
    totalAmount += lineTotal;

    return {productId, qty, unitPrice, lineTotal};
  });

  // 4) Create Firestore order (server authoritative fields)
  const orderRef = admin.firestore().collection("orders").doc();
  const now = admin.firestore.FieldValue.serverTimestamp();

  const order = {
    id: orderRef.id,
    buyerId,
    sellerId,
    status: "PLACED",
    items: normalizedItems,
    totalAmount,
    createdAt: now,
    updatedAt: now,
  };

  await orderRef.set(order);

  // 5) Return order id to client
  return {ok: true, orderId: orderRef.id};
});
