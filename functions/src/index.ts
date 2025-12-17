import * as functions from "firebase-functions/v1";
import * as admin from "firebase-admin";

admin.initializeApp();

export const onAuthUserCreate = functions.auth.user().onCreate(
  async (user: admin.auth.UserRecord) => {
    await admin.firestore().collection("users").doc(user.uid).set(
      {
        role: "buyer",
        status: "active",
      },
      { merge: true }
    );
    return null;
  }
);
