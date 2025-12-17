import * as functions from "firebase-functions/v1";
import * as admin from "firebase-admin";

admin.initializeApp();

/**
 * Auth trigger: create/merge a Firestore user profile on signup.
 *
 * Path: /users/{uid}
 *
 * @param {admin.auth.UserRecord} user Auth user record.
 * @return {Promise<null>} Resolves when the profile is written.
 */
export const onAuthUserCreate = functions.auth.user().onCreate(
  async (user: admin.auth.UserRecord): Promise<null> => {
    await admin.firestore().collection("users").doc(user.uid).set(
      {role: "buyer", status: "active"},
      {merge: true}
    );
    return null;
  }
);

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
 * Convert unknown to string; non-strings become "".
 *
 * @param {unknown} value Any input value.
 * @return {string} The value if string, otherwise "".
 */
function asString(value: unknown): string {
  return typeof value === "string" ? value : "";
}

/**
 * Convert unknown to number using Number(...).
 *
 * Note: invalid values become NaN; caller should validate.
 *
 * @param {unknown} value Any input value.
 * @return {number} A number (may be NaN).
 */
function asNumber(value: unknown): number {
  return typeof value === "number" ? value : Number(value);
}

/**
 * Type guard: validate item payload shape.
 *
 * @param {unknown} value Any input value.
 * @return {boolean} True if value matches CreateOrderItemInput.
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
 * Callable function: buyer creates an order.
 *
 * Requires authentication and valid payload.
 *
 * @param {unknown} data Callable request payload.
 * @param {functions.https.CallableContext} context Callable context.
 * @return {Promise<{ok:boolean,orderId:string}>} Creation result.
 */
export const createOrder = functions.https.onCall(
  async (
    data: unknown,
    context: functions.https.CallableContext
  ): Promise<{ok: boolean, orderId: string}> => {
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

      return {productId: productId, qty: qty, unitPrice: unitPrice,
        lineTotal: lineTotal};
    });

    const orderRef = admin.firestore().collection("orders").doc();
    const now = admin.firestore.FieldValue.serverTimestamp();

    const order = {
      id: orderRef.id,
      buyerId: context.auth.uid,
      sellerId: sellerId,
      status: "PLACED",
      items: normalizedItems,
      totalAmount: totalAmount,
      createdAt: now,
      updatedAt: now,
    };

    await orderRef.set(order);

    return {ok: true, orderId: orderRef.id};
  }
);
