import * as functions from "firebase-functions/v1";
import * as admin from "firebase-admin";

admin.initializeApp();

/**
 * Helpers
 */
function asString(value: unknown): string {
  return typeof value === "string" ? value : "";
}

function asNumber(value: unknown): number {
  return typeof value === "number" ? value : Number(value);
}

function requireAuth(context: functions.https.CallableContext): string {
  if (!context.auth) {
    throw new functions.https.HttpsError("unauthenticated", "Login required.");
  }
  return context.auth.uid;
}

function requireAppCheck(context: functions.https.CallableContext): void {
  // Enforces Firebase App Check on callable functions
  if (!context.app) {
    throw new functions.https.HttpsError(
      "failed-precondition",
      "App Check required."
    );
  }
}

/**
 * 1) Auth trigger: create a base Firestore user profile on signup.
 * Path: /users/{uid}
 *
 * IMPORTANT:
 * - Do NOT guess buyer/seller here.
 * - Apps will set role explicitly right after signup.
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
      { merge: true }
    );
    return null;
  }
);

/**
 * 2) Callable: set role right after signup
 * Buyer app calls:  setRoleAfterSignup({ role: "buyer" })
 * Seller app calls: setRoleAfterSignup({ role: "seller" })
 */
type SetRoleInput = { role: "buyer" | "seller" };

export const setRoleAfterSignup = functions.https.onCall(
  async (
    data: unknown,
    context: functions.https.CallableContext
  ): Promise<{ ok: boolean; role: "buyer" | "seller" }> => {
    const uid = requireAuth(context);
    requireAppCheck(context);

    // Use the type (prevents unused-type issues and keeps it strict)
    const input = data as Partial<SetRoleInput>;
    const role = input.role;

    if (role !== "buyer" && role !== "seller") {
      throw new functions.https.HttpsError("invalid-argument", "Invalid role.");
    }

    // Firestore: source of truth for UI
    await admin.firestore().collection("users").doc(uid).set(
      {
        role,
        updatedAt: admin.firestore.FieldValue.serverTimestamp(),
      },
      { merge: true }
    );

    // Custom claims: for security rules + backend checks
    await admin.auth().setCustomUserClaims(uid, { role });

    return { ok: true, role };
  }
);

/**
 * 3) OPTIONAL: Invite-code upgrade to seller
 * Use only if you want seller access control (invite codes).
 */
type UpgradeToSellerInput = { inviteCode: string };

export const upgradeToSeller = functions.https.onCall(
  async (
    data: unknown,
    context: functions.https.CallableContext
  ): Promise<{ ok: boolean; role: "seller"; storeId?: string; storeName?: string }> => {
    const uid = requireAuth(context);
    requireAppCheck(context);

    // Use the type (fixes TS6196)
    const input = data as Partial<UpgradeToSellerInput>;
    const inviteCode = asString(input.inviteCode).trim();

    if (!inviteCode) {
      throw new functions.https.HttpsError(
        "invalid-argument",
        "inviteCode is required."
      );
    }

    const inviteRef = admin.firestore().collection("seller_invites").doc(inviteCode);
    const userRef = admin.firestore().collection("users").doc(uid);

    const result = await admin.firestore().runTransaction(async (tx) => {
      const inviteSnap = await tx.get(inviteRef);
      if (!inviteSnap.exists) {
        throw new functions.https.HttpsError("not-found", "Invalid invite code.");
      }

      const invite = inviteSnap.data() as any;

      if (invite.active !== true) {
        throw new functions.https.HttpsError(
          "failed-precondition",
          "Invite code is not active."
        );
      }

      const maxUses = typeof invite.maxUses === "number" ? invite.maxUses : 1;
      const usedCount = typeof invite.usedCount === "number" ? invite.usedCount : 0;

      if (usedCount >= maxUses) {
        throw new functions.https.HttpsError(
          "resource-exhausted",
          "Invite code already used."
        );
      }

      // Optional expiry check
      const expiresAt = invite.expiresAt;
      if (expiresAt?.toMillis && expiresAt.toMillis() < Date.now()) {
        throw new functions.https.HttpsError("deadline-exceeded", "Invite code expired.");
      }

      const storeId = typeof invite.storeId === "string" ? invite.storeId : "";
      const storeName = typeof invite.storeName === "string" ? invite.storeName : "";

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
        { merge: true }
      );

      return { storeId, storeName };
    });

    await admin.auth().setCustomUserClaims(uid, { role: "seller" });

    return {
      ok: true,
      role: "seller",
      storeId: result.storeId || undefined,
      storeName: result.storeName || undefined,
    };
  }
);

/**
 * 4) createOrder (MVP)
 * NOTE: next improvement: storeId-based order path, and server-side price verification.
 */
type CreateOrderItemInput = {
  productId: string;
  qty: number;
  unitPrice: number;
};

type CreateOrderInput = {
  sellerId: string; // for now; later move to storeId
  items: CreateOrderItemInput[];
};

function isItemInput(value: unknown): value is CreateOrderItemInput {
  if (!value || typeof value !== "object") return false;
  const v = value as Record<string, unknown>;
  return (
    typeof v.productId === "string" &&
    typeof v.qty !== "undefined" &&
    typeof v.unitPrice !== "undefined"
  );
}

export const createOrder = functions.https.onCall(
  async (
    data: unknown,
    context: functions.https.CallableContext
  ): Promise<{ ok: boolean; orderId: string }> => {
    const buyerId = requireAuth(context);
    requireAppCheck(context);

    const input = data as Partial<CreateOrderInput>;
    const sellerId = asString(input.sellerId).trim();
    const rawItems = input.items;

    if (!sellerId) {
      throw new functions.https.HttpsError("invalid-argument", "sellerId is required.");
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

    await orderRef.set({
      id: orderRef.id,
      buyerId,
      sellerId,
      status: "PLACED",
      items: normalizedItems,
      totalAmount,
      createdAt: now,
      updatedAt: now,
    });

    return { ok: true, orderId: orderRef.id };
  }
);
