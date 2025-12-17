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
type CreateOrderItemInput = {
  productId: string;
  qty: number;
  unitPrice: number;
};

type CreateOrderInput = {
  sellerId: string;
  items: CreateOrderItemInput[];
};

function asString(value: unknown): string {
  return typeof value === "string" ? value : "";
}

function asNumber(value: unknown): number {
  return typeof value === "number" ? value : Number(value);
}

function isItemInput(value: unknown): value is CreateOrderItemInput {
  if (!value || typeof value !== "object") return false;
  const v = value as Record<string, unknown>;
  return (
    typeof v.productId === "string" &&
    typeof v.qty !== "undefined" &&
    typeof v.unitPrice !== "undefined"
  );
}

export const createOrder = functions.https.onCall(async (data, context) => {
  if (!context.auth) {
    throw new functions.https.HttpsError(
      "unauthenticated",
      "Login required."
    );
  }

  const input = data as CreateOrderInput;
  const sellerId = asString(input?.sellerId).trim();
  const rawItems = input?.items;

  if (!sellerId) {
    throw new functions.https.HttpsError(
      "invalid-argument",
      "sellerId is required."
    );
  }

  if (!Array.isArray(rawItems) || rawItems.length === 0) {
    throw new functions.https.HttpsError(
      "invalid-argument",
      "items must be a non-empty array."
    );
  }

  let totalAmount = 0;

  const normalizedItems = rawItems.map((raw) => {
    if (!isItemInput(raw)) {
      throw new functions.https.HttpsError(
        "invalid-argument",
        "Each item must have productId, qty, unitPrice."
      );
    }

    const productId = raw.productId.trim();
    const qty = asNumber(raw.qty);
    const unitPrice = asNumber(raw.unitPrice);

    if (!productId) {
      throw new functions.https.HttpsError(
        "invalid-argument",
        "productId is required."
      );
    }

    if (!Number.isFinite(qty) || qty <= 0) {
      throw new functions.https.HttpsError(
        "invalid-argument",
        "qty must be > 0."
      );
    }

    if (!Number.isFinite(unitPrice) || unitPrice < 0) {
      throw new functions.https.HttpsError(
        "invalid-argument",
        "unitPrice must be >= 0."
      );
    }

    const lineTotal = qty * unitPrice;
    totalAmount += lineTotal;

    return {productId, qty, unitPrice, lineTotal};
  });

  const orderRef = admin.firestore().collection("orders").doc();
  const now = admin.firestore.FieldValue.serverTimestamp();

  const order = {
    id: orderRef.id,
    buyerId: context.auth.uid,
    sellerId,
    status: "PLACED",
    items: normalizedItems,
    totalAmount,
    createdAt: now,
    updatedAt: now,
  };

  await orderRef.set(order);

  return {ok: true, orderId: orderRef.id};
});
