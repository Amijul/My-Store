import * as functions from "firebase-functions";
import * as admin from "firebase-admin";

admin.initializeApp();

/**
 * Trigger: Runs when a new Firebase Auth user is created.
 * Action: Creates /users/{uid} in Firestore with minimal profile fields.
 */
export const onAuthUserCreate = functions.auth.user().onCreate(async (user) => {
  const uid = user.uid;

  await admin.firestore().collection("users").doc(uid).set(
    {
      role: "buyer",
      status: "active",
    },
    { merge: true } // safe + idempotent
  );

  return null;
});
