import * as functions from "firebase-functions/v1";
import * as admin from "firebase-admin";

admin.initializeApp();

/**
 * Auth trigger: creates/merges a Firestore user profile when a new Firebase Auth user signs up.
 *
 * Creates: /users/{uid}
 * Default fields: role=buyer, status=active
 */
export const onAuthUserCreate = functions.auth.user().onCreate(
  async (user: admin.auth.UserRecord) => {
    await admin.firestore().collection("users").doc(user.uid).set(
      { role: "buyer", status: "active" },
      { merge: true }
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

/**
 * Safely converts an unknown input to a string. Returns empty string if not a string.
 *
 * @param value Any input value
 * @returns A string value or "" if input is not a string
 */
function asString(value: unknown): string {
  return typeof value === "string" ? value : "";
}

/**
 * Safely converts an unknown input to a number using Number(...).
 *
 * Note: Non-numeric strings will become NaN. Caller should validate with Number.isFinite(...).
 *
 * @param value Any input value
 * @returns A number (may be NaN)
 */
function asNumber(value: unknown): number {
  return typeof value === "number" ? value : Number(value);
}

/**
 * Type guard for validating item inputs received from the client.
 *
 * @param value Any input value
 * @returns True if value is a CreateOrderItemInput-like object
 */
function isItemInput(value: unknown): value is CreateOrderItemInput {
  if (!value || typeof value !== "object") return false;
  const v = value as Record<string, unknown>;
  return (
    typeof v.productId === "string" &&
    typeof v.qty !== "undefined" &&
    typeof v.unitPrice !== "undefined"
  );
}

/**
 * Callable HTTPS function for buyers to create an order.
 *
 * Required:
 * - Authenticated user (context.auth)
 * - data.sellerId: string
 * - data.items: array of { productId, qty, unitPrice }
 *
 * @param data Callable request payload
 * @param context Callable function context (includes auth)
 * @returns {ok: true, orderId} on success
 * @throws HttpsError for unauthenticated / invalid-argument cases
 */
export const createOrder = functions.https.onCall(async (data, context) => {
  if (!context.auth) {
    throw new functions.https.HttpsError("unauthenticated", "Login required.");
  }

  const input = data as CreateOrderInput;
  const sellerId = asString(input?.sellerId).trim();
  const rawItems = input?.items;

  if (!sellerId) {
    throw new functions.https.HttpsError("invalid-argument", "sellerId is required.");
  }

  if (!Array.isArray(rawItems) || rawItems.length === 0) {
    throw new functions.https.HttpsError("invalid-argument", "items must be a non-empty array.");
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
      throw new functions.https.HttpsError("invalid-argument", "productId is required.");
    }

    if (!Number.isFinite(qty) || qty <= 0) {
      throw new functions.https.HttpsError("invalid-argument", "qty must be > 0.");
    }

    if (!Number.isFinite(unitPrice) || unitPrice < 0) {
      throw new functions.https.HttpsError("invalid-argument", "unitPrice must be >= 0.");
    }

    const lineTotal = qty * unitPrice;
    totalAmount += lineTotal;

    return { productId, qty, unitPrice, lineTotal };
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

  return { ok: true, orderId: orderRef.id };
});
