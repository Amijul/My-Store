import * as functions from "firebase-functions/v1";
import * as admin from "firebase-admin";

admin.initializeApp();

/**
 * Helpers
 */
function asString(value: unknown): string {
  return typeof value === "string" ? value : "";
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
 * 2) Callable: set role right after signup (Buyer app -> buyer, Seller app -> seller).
 *
 * Buyer app calls:  setRoleAfterSignup({ role: "buyer" })
 * Seller app calls: setRoleAfterSignup({ role: "seller" })
 *
 * Requires:
 * - Auth
 * - App Check
 */
type SetRoleInput = { role: "buyer" | "seller" };

export const setRoleAfterSignup = functions.https.onCall(
  async (
    data: unknown,
    context: functions.https.CallableContext
  ): Promise<{ ok: boolean; role: "buyer" | "seller" }> => {
    const uid = requireAuth(context);
    requireAppCheck(context);

    const role = (data as any)?.role as SetRoleInput["role"];
    if (role !== "buyer" && role !== "seller") {
      throw new functions.https.HttpsError("invalid-argument", "Invalid role.");
    }

    // Write Firestore role (source of truth for UI)
    await admin.firestore().collection("users").doc(uid).set(
      {
        role,
        updatedAt: admin.firestore.FieldValue.serverTimestamp(),
      },
      { merge: true }
    );

    // Set custom claims (useful for Security Rules and server-side checks)
    await admin.auth().setCustomUserClaims(uid, { role });

    return { ok: true, role };
  }
);

/**
 * 3) OPTIONAL: Invite-code upgrade to seller (use only if you want seller access control).
 *
 * If you plan "separate seller app = always seller", you may NOT need this.
 * Keep it only if you want to restrict seller creation using codes.
 */
type UpgradeToSellerInput = { inviteCode: string };

export const upgradeToSeller = functions.https.onCall(
  async (
    data: unknown,
    context: functions.https.CallableContext
  ): Promise<{ ok: boolean; role: "seller"; storeId?: string; storeName?: string }> => {
    const uid = requireAuth(context);
    requireAppCheck(context);

    const inviteCode = asString((data as any)?.inviteCode).trim();
    if (!inviteCode) {
      throw new functions.https.HttpsError("invalid-argument", "inviteCode is required.");
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
        throw new functions.https.HttpsError("failed-precondition", "Invite code is not active.");
      }

      const maxUses = typeof invite.maxUses === "number" ? invite.maxUses : 1;
      const usedCount = typeof invite.usedCount === "number" ? invite.usedCount : 0;

      if (usedCount >= maxUses) {
        throw new functions.https.HttpsError("resource-exhausted", "Invite code already used.");
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
 * 4) Your createOrder: leave as-is for now (but next step should be storeId-based).
 * NOTE: This function is OK for MVP, but later we should change sellerId -> storeId
 * and write orders under /stores/{storeId}/orders/{orderId}
 */

// Keep your existing createOrder code below if you want unchanged.
// (I did not rewrite it here because you said "need only and clean easy to understand".)
