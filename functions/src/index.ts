import * as functions from "firebase-functions/v1";
import * as admin from "firebase-admin";

admin.initializeApp();

/**
 * Convert unknown to string.
 *
 * @param {unknown} value Any value.
 * @return {string} String value or "".
 */
function asString(value: unknown): string {
  return typeof value === "string" ? value : "";
}

/**
 * Convert unknown to number.
 *
 * @param {unknown} value Any value.
 * @return {number} Number value (may be NaN).
 */
function asNumber(value: unknown): number {
  return typeof value === "number" ? value : Number(value);
}

/**
 * Require authenticated callable context.
 *
 * @param {functions.https.CallableContext} context Callable context.
 * @return {string} UID of the caller.
 */
function requireAuth(
  context: functions.https.CallableContext
): string {
  if (!context.auth) {
    throw new functions.https.HttpsError(
      "unauthenticated",
      "Login required."
    );
  }
  return context.auth.uid;
}

/**
 * Require Firebase App Check for callable functions.
 *
 * @param {functions.https.CallableContext} context Callable context.
 * @return {void}
 */
function requireAppCheck(
  context: functions.https.CallableContext
): void {
  if (!context.app) {
    throw new functions.https.HttpsError(
      "failed-precondition",
      "App Check required."
    );
  }
}

/**
 * 1) Auth trigger: create base Firestore user profile on signup.
 *
 * Path: /users/{uid}
 *
 * IMPORTANT:
 * - Do NOT guess buyer/seller here.
 * - Apps will set role explicitly right after signup.
 *
 * @param {admin.auth.UserRecord} user Auth user record.
 * @return {Promise<null>} Null.
 */
export const onAuthUserCreate = functions.auth.user().onCreate(
  async (user: admin.auth.UserRecord): Promise<null> => {
    await admin.firestore().collection("users").doc(user.uid).set(
      {
        role: "unknown",
        status: "active",
        email: user.email ?? null,
        createdAt: admin.firestore.FieldValue.serverTimestamp(),
        updatedAt: admin.firestore.FieldValue.serverTimestamp(),
      },
      {merge: true}
    );
    return null;
  }
);

/**
 * Callable payload for setting role.
 */
type SetRoleInput = {role: "buyer" | "seller"};

/**
 * 2) Callable: set role right after signup.
 *
 * Buyer app calls:  setRoleAfterSignup({role: "buyer"})
 * Seller app calls: setRoleAfterSignup({role: "seller"})
 *
 * Requires:
 * - Auth
 * - App Check
 *
 * @param {unknown} data Callable request payload.
 * @param {functions.https.CallableContext} context Callable context.
 * @return {Promise<{ok:boolean,role:"buyer"|"seller"}>} Result.
 */
export const setRoleAfterSignup = functions.https.onCall(
  async (
    data: unknown,
    context: functions.https.CallableContext
  ): Promise<{ok: boolean; role: "buyer" | "seller"}> => {
    const uid = requireAuth(context);
    requireAppCheck(context);

    const input = data as Partial<SetRoleInput>;
    const role = input.role;

    if (role !== "buyer" && role !== "seller") {
      throw new functions.https.HttpsError(
        "invalid-argument",
        "Invalid role."
      );
    }

    await admin.firestore().collection("users").doc(uid).set(
      {
        role: role,
        updatedAt: admin.firestore.FieldValue.serverTimestamp(),
      },
      {merge: true}
    );

    await admin.auth().setCustomUserClaims(uid, {role: role});

    return {ok: true, role: role};
  }
);

/**
 * Callable payload for upgrading to seller.
 */
type UpgradeToSellerInput = {inviteCode: string};

/**
 * 3) OPTIONAL: invite-code upgrade to seller.
 *
 * Keep only if you want seller access control using codes.
 *
 * @param {unknown} data Callable request payload.
 * @param {functions.https.CallableContext} context Callable context.
 * @return {Promise<{ok:boolean,role:"seller",storeId?:string,storeName?:string}>}
 * Result.
 */
export const upgradeToSeller = functions.https.onCall(
  async (
    data: unknown,
    context: functions.https.CallableContext
  ): Promise<{
    ok: boolean;
    role: "seller";
    storeId?: string;
    storeName?: string;
  }> => {
    const uid = requireAuth(context);
    requireAppCheck(context);

    const input = data as Partial<UpgradeToSellerInput>;
    const inviteCode = asString(input.inviteCode).trim();

    if (!inviteCode) {
      throw new functions.https.HttpsError(
        "invalid-argument",
        "inviteCode is required."
      );
    }

    const inviteRef = admin.firestore()
      .collection("seller_invites")
      .doc(inviteCode);
    const userRef = admin.firestore()
      .collection("users")
      .doc(uid);

    const result = await admin.firestore().runTransaction(async (tx) => {
      const inviteSnap = await tx.get(inviteRef);
      if (!inviteSnap.exists) {
        throw new functions.https.HttpsError(
          "not-found",
          "Invalid invite code."
        );
      }

      const invite = inviteSnap.data() as Record<string, unknown>;

      if (invite.active !== true) {
        throw new functions.https.HttpsError(
          "failed-precondition",
          "Invite code is not active."
        );
      }

      const maxUses = typeof invite.maxUses === "number"
        ? invite.maxUses
        : 1;
      const usedCount = typeof invite.usedCount === "number"
        ? invite.usedCount
        : 0;

      if (usedCount >= maxUses) {
        throw new functions.https.HttpsError(
          "resource-exhausted",
          "Invite code already used."
        );
      }

      const expiresAt = invite.expiresAt as
        | admin.firestore.Timestamp
        | undefined;

      if (expiresAt && expiresAt.toMillis() < Date.now()) {
        throw new functions.https.HttpsError(
          "deadline-exceeded",
          "Invite code expired."
        );
      }

      const storeId = typeof invite.storeId === "string"
        ? invite.storeId
        : "";
      const storeName = typeof invite.storeName === "string"
        ? invite.storeName
        : "";

      tx.update(inviteRef, {
        usedCount: usedCount + 1,
        usedBy: admin.firestore.FieldValue.arrayUnion(uid),
        lastUsedAt: admin.firestore.FieldValue.serverTimestamp(),
      });

      tx.set(
        userRef,
        {
          role: "seller",
          status: "active",
          storeId: storeId || null,
          storeName: storeName || null,
          updatedAt: admin.firestore.FieldValue.serverTimestamp(),
        },
        {merge: true}
      );

      return {storeId: storeId, storeName: storeName};
    });

    await admin.auth().setCustomUserClaims(uid, {role: "seller"});

    return {
      ok: true,
      role: "seller",
      storeId: result.storeId || undefined,
      storeName: result.storeName || undefined,
    };
  }
);

/**
 * createOrder item input.
 */
type CreateOrderItemInput = {
  productId: string;
  qty: number;
  unitPrice: number;
};

/**
 * createOrder input.
 */
type CreateOrderInput = {
  sellerId: string;
  items: CreateOrderItemInput[];
};

/**
 * Type guard for order items.
 *
 * @param {unknown} value Any value.
 * @return {boolean} True if CreateOrderItemInput.
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
 * 4) Callable: buyer creates an order (MVP).
 *
 * @param {unknown} data Callable request payload.
 * @param {functions.https.CallableContext} context Callable context.
 * @return {Promise<{ok:boolean,orderId:string}>} Result.
 */
export const createOrder = functions.https.onCall(
  async (
    data: unknown,
    context: functions.https.CallableContext
  ): Promise<{ok: boolean; orderId: string}> => {
    const buyerId = requireAuth(context);
    requireAppCheck(context);

    const input = data as Partial<CreateOrderInput>;
    const sellerId = asString(input.sellerId).trim();
    const rawItems = input.items;

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

      return {
        productId: productId,
        qty: qty,
        unitPrice: unitPrice,
        lineTotal: lineTotal,
      };

    });

    const orderRef = admin.firestore().collection("orders").doc();
    const now = admin.firestore.FieldValue.serverTimestamp();

    await orderRef.set({
      id: orderRef.id,
      buyerId: buyerId,
      sellerId: sellerId,
      status: "PLACED",
      items: normalizedItems,
      totalAmount: totalAmount,
      createdAt: now,
      updatedAt: now,
    });

    return {ok: true, orderId: orderRef.id};
  }
);
