import * as functions from "firebase-functions/v1";
import * as admin from "firebase-admin";

admin.initializeApp();

/**
 * Helpers
 *
 * @param {unknown} value Any value.
 * @return {string} String or "".
 */
function asString(value: unknown): string {
  return typeof value === "string" ? value : "";
}

/**
 * @param {unknown} value Any value.
 * @return {number} Number (may be NaN).
 */
function asNumber(value: unknown): number {
  return typeof value === "number" ? value : Number(value);
}

/**
 * @param {functions.https.CallableContext} context Callable context.
 * @return {string} UID.
 */
function requireAuth(context: functions.https.CallableContext): string {
  if (!context.auth) {
    throw new functions.https.HttpsError(
      "unauthenticated",
      "Login required."
    );
  }
  return context.auth.uid;
}

/**
 * 1) Auth trigger: create base profile at /users/{uid}.
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
        storeId: null,
        storeName: null,
        createdAt: admin.firestore.FieldValue.serverTimestamp(),
        updatedAt: admin.firestore.FieldValue.serverTimestamp(),
      },
      {merge: true}
    );
    return null;
  }
);

/**
 * 2) Callable: set role right after signup.
 */
type SetRoleInput = {role: "buyer" | "seller"};

/**
 * @param {unknown} data Callable payload.
 * @param {functions.https.CallableContext} context Callable context.
 * @return {Promise<{ok:boolean,role:"buyer"|"seller"}>} Result.
 */
export const setRoleAfterSignup = functions.https.onCall(
  async (
    data: unknown,
    context: functions.https.CallableContext
  ): Promise<{ok: boolean; role: "buyer" | "seller"}> => {
    const uid = requireAuth(context);
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
 * Store update input (seller updates own store profile).
 */
type UpdateMyStoreInput = {
  name: string;
  phone?: string;
  type: "grocery" | "sweets" | "clothes" | "pharmacy" | "other";
  isActive: boolean;
  address?: {
    line1?: string;
    city?: string;
    state?: string;
    pincode?: string;
  };
};

/**
 * Update seller's own store profile (owner-only).
 *
 * @param {unknown} data Callable payload.
 * @param {functions.https.CallableContext} context Callable context.
 * @return {Promise<{ok:boolean}>} Result.
 */
export const updateMyStoreProfile = functions.https.onCall(
  async (
    data: unknown,
    context: functions.https.CallableContext
  ): Promise<{ok: boolean}> => {
    const uid = requireAuth(context);

    const userRef = admin.firestore().collection("users").doc(uid);
    const userSnap = await userRef.get();
    const role = asString(userSnap.get("role"));
    const storeId = asString(userSnap.get("storeId"));

    if (role !== "seller" || !storeId) {
      throw new functions.https.HttpsError(
        "permission-denied",
        "Seller store not found."
      );
    }

    const input = data as Partial<UpdateMyStoreInput>;
    const name = asString(input.name).trim();
    const phone = asString(input.phone).trim();
    const type = input.type;
    const isActive = input.isActive === true;

    if (!name) {
      throw new functions.https.HttpsError(
        "invalid-argument",
        "name is required."
      );
    }

    const allowed =
      type === "grocery" ||
      type === "sweets" ||
      type === "clothes" ||
      type === "pharmacy" ||
      type === "other";

    if (!allowed) {
      throw new functions.https.HttpsError(
        "invalid-argument",
        "Invalid store type."
      );
    }

    const storeRef = admin.firestore().collection("stores").doc(storeId);
    const storeSnap = await storeRef.get();

    if (!storeSnap.exists) {
      throw new functions.https.HttpsError(
        "not-found",
        "Store does not exist."
      );
    }

    const ownerUid = asString(storeSnap.get("ownerUid"));
    if (ownerUid !== uid) {
      throw new functions.https.HttpsError(
        "permission-denied",
        "Not store owner."
      );
    }

    const now = admin.firestore.FieldValue.serverTimestamp();

    await storeRef.set(
      {
        name: name,
        type: type,
        phone: phone || null,
        isActive: isActive,
        address: {
          line1: asString(input.address?.line1).trim() || null,
          city: asString(input.address?.city).trim() || null,
          state: asString(input.address?.state).trim() || null,
          pincode: asString(input.address?.pincode).trim() || null,
        },
        updatedAt: now,
      },
      {merge: true}
    );

    await userRef.set(
      {
        storeName: name,
        updatedAt: now,
      },
      {merge: true}
    );

    return {ok: true};
  }
);

/**
 * 3) Create store for seller (1 seller = 1 store), idempotent.
 */
type CreateStoreInput = {
  name: string;
  type: "grocery" | "sweets" | "clothes" | "pharmacy" | "other";
  phone?: string;
  address?: {
    line1?: string;
    city?: string;
    state?: string;
    pincode?: string;
  };
};

/**
 * @param {unknown} data Callable payload.
 * @param {functions.https.CallableContext} context Callable context.
 * @return {Promise<{ok:boolean,storeId:string}>} Result.
 */
export const createStoreForSeller = functions.https.onCall(
  async (
    data: unknown,
    context: functions.https.CallableContext
  ): Promise<{ok: boolean; storeId: string}> => {
    const uid = requireAuth(context);

    const userRef = admin.firestore().collection("users").doc(uid);
    const userSnap = await userRef.get();
    const role = asString(userSnap.get("role"));

    if (role !== "seller") {
      throw new functions.https.HttpsError(
        "permission-denied",
        "Only seller can create a store."
      );
    }

    const existingStoreId = asString(userSnap.get("storeId"));
    if (existingStoreId) {
      return {ok: true, storeId: existingStoreId};
    }

    const input = data as Partial<CreateStoreInput>;
    const name = asString(input.name).trim();
    const type = input.type;

    if (!name) {
      throw new functions.https.HttpsError(
        "invalid-argument",
        "name is required."
      );
    }

    const allowed =
      type === "grocery" ||
      type === "sweets" ||
      type === "clothes" ||
      type === "pharmacy" ||
      type === "other";

    if (!allowed) {
      throw new functions.https.HttpsError(
        "invalid-argument",
        "Invalid store type."
      );
    }

    const storeRef = admin.firestore().collection("stores").doc();
    const now = admin.firestore.FieldValue.serverTimestamp();

    await admin.firestore().runTransaction(async (tx) => {
      tx.set(storeRef, {
        storeId: storeRef.id,
        ownerUid: uid,
        name: name,
        type: type,
        phone: asString(input.phone).trim() || null,
        address: {
          line1: asString(input.address?.line1).trim() || null,
          city: asString(input.address?.city).trim() || null,
          state: asString(input.address?.state).trim() || null,
          pincode: asString(input.address?.pincode).trim() || null,
        },
        isActive: true,
        createdAt: now,
        updatedAt: now,
      });

      tx.set(
        userRef,
        {
          storeId: storeRef.id,
          storeName: name,
          updatedAt: now,
        },
        {merge: true}
      );
    });

    return {ok: true, storeId: storeRef.id};
  }
);

/**
 * 4) Upsert product (seller only, within own store).
 *
 * MVP decision:
 * - unit is a simple string ("pcs", "kg", "g", "l", "ml", "pack").
 */
type UpsertProductInput = {
  productId?: string;
  category: "grocery" | "sweets" | "clothes" | "pharmacy" | "other";
  subCategory?: string;

  name: string;
  description?: string;

  thumbnail?: string;
  imageUrl?: string;

  currency?: "INR";
  price: number;

  inStock?: boolean;
  stockQty?: number;

  unit?: string;

  isActive?: boolean;
};

/**
 * @param {unknown} data Callable payload.
 * @param {functions.https.CallableContext} context Callable context.
 * @return {Promise<{ok:boolean,storeId:string,productId:string}>} Result.
 */
export const upsertProduct = functions.https.onCall(
  async (
    data: unknown,
    context: functions.https.CallableContext
  ): Promise<{ok: boolean; storeId: string; productId: string}> => {
    const uid = requireAuth(context);

    const userRef = admin.firestore().collection("users").doc(uid);
    const userSnap = await userRef.get();
    const role = asString(userSnap.get("role"));
    const storeId = asString(userSnap.get("storeId"));

    if (role !== "seller" || !storeId) {
      throw new functions.https.HttpsError(
        "permission-denied",
        "Seller store not found."
      );
    }

    const input = data as Partial<UpsertProductInput>;
    const name = asString(input.name).trim();
    const category = input.category;

    if (!name) {
      throw new functions.https.HttpsError(
        "invalid-argument",
        "name is required."
      );
    }

    const allowedCategory =
      category === "grocery" ||
      category === "sweets" ||
      category === "clothes" ||
      category === "pharmacy" ||
      category === "other";

    if (!allowedCategory) {
      throw new functions.https.HttpsError(
        "invalid-argument",
        "Invalid category."
      );
    }

    const price = asNumber(input.price);
    if (!Number.isFinite(price) || price < 0) {
      throw new functions.https.HttpsError(
        "invalid-argument",
        "Invalid price."
      );
    }

    const productId = asString(input.productId).trim();
    const productsCol = admin.firestore()
      .collection("stores")
      .doc(storeId)
      .collection("products");

    const productRef = productId ? productsCol.doc(productId) : productsCol.doc();
    const now = admin.firestore.FieldValue.serverTimestamp();

    const unit = asString(input.unit).trim();
    const thumb = asString(input.thumbnail).trim();
    const imageUrl = asString(input.imageUrl).trim();

    const doc: Record<string, unknown> = {
      productId: productRef.id,
      storeId: storeId,
      category: category,
      subCategory: asString(input.subCategory).trim() || null,

      name: name,
      description: asString(input.description).trim() || null,

      thumbnail: (thumb || imageUrl) || null,
      imageUrl: (thumb || imageUrl) || null,

      currency: input.currency || "INR",
      price: price,

      inStock: typeof input.inStock === "boolean" ? input.inStock : true,
      stockQty: Number.isFinite(asNumber(input.stockQty))
        ? asNumber(input.stockQty)
        : null,

      unit: unit || null,

      isActive: typeof input.isActive === "boolean" ? input.isActive : true,
      updatedAt: now,
    };

    if (!productId) {
      doc.createdAt = now;
    }

    await productRef.set(doc, {merge: true});

    return {ok: true, storeId: storeId, productId: productRef.id};
  }
);

/**
 * 5) Create order: buyer places order for a store.
 * Server reads prices from /stores/{storeId}/products/{productId}.
 */
type CreateOrderItemInput = {
  productId: string;
  qty: number;
};

type CreateOrderInput = {
  storeId: string;
  items: CreateOrderItemInput[];
};

/**
 * @param {unknown} data Callable payload.
 * @param {functions.https.CallableContext} context Callable context.
 * @return {Promise<{ok:boolean,orderId:string}>} Result.
 */
export const createOrder = functions.https.onCall(
  async (
    data: unknown,
    context: functions.https.CallableContext
  ): Promise<{ok: boolean; orderId: string}> => {
    const buyerId = requireAuth(context);

    const input = data as Partial<CreateOrderInput>;
    const storeId = asString(input.storeId).trim();
    const rawItems = input.items;

    if (!storeId) {
      throw new functions.https.HttpsError(
        "invalid-argument",
        "storeId is required."
      );
    }

    if (!Array.isArray(rawItems) || rawItems.length === 0) {
      throw new functions.https.HttpsError(
        "invalid-argument",
        "items must be non-empty."
      );
    }

    const storeRef = admin.firestore().collection("stores").doc(storeId);
    const storeSnap = await storeRef.get();

    if (!storeSnap.exists) {
      throw new functions.https.HttpsError(
        "not-found",
        "Store not found."
      );
    }

    const storeName = asString(storeSnap.get("name"));

    let itemsTotal = 0;
    const normalizedItems: Array<Record<string, unknown>> = [];

    for (const raw of rawItems) {
      const productId = asString((raw as Record<string, unknown>)?.productId)
        .trim();
      const qty = asNumber((raw as Record<string, unknown>)?.qty);

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

      const productRef = storeRef.collection("products").doc(productId);
      const productSnap = await productRef.get();

      if (!productSnap.exists) {
        throw new functions.https.HttpsError(
          "not-found",
          "Product not found."
        );
      }

      const pStoreId = asString(productSnap.get("storeId"));
      if (pStoreId && pStoreId !== storeId) {
        throw new functions.https.HttpsError(
          "failed-precondition",
          "Product-store mismatch."
        );
      }

      const inStock = productSnap.get("inStock");
      if (inStock === false) {
        throw new functions.https.HttpsError(
          "failed-precondition",
          "Product out of stock."
        );
      }

      const unitPrice = Number(productSnap.get("price") ?? 0);
      if (!Number.isFinite(unitPrice) || unitPrice < 0) {
        throw new functions.https.HttpsError(
          "failed-precondition",
          "Invalid product price."
        );
      }

      const name = asString(productSnap.get("name"));
      const imageUrl = asString(productSnap.get("thumbnail")) ||
        asString(productSnap.get("imageUrl"));

      const lineTotal = unitPrice * qty;
      itemsTotal += lineTotal;

      normalizedItems.push({
        productId: productId,
        name: name,
        imageUrl: imageUrl,
        qty: qty,
        unitPrice: unitPrice,
        lineTotal: lineTotal,
      });
    }

    const shipping = normalizedItems.length === 0 ? 0 : 40;
    const grandTotal = itemsTotal + shipping;

    const orderRef = admin.firestore().collection("orders").doc();
    const now = admin.firestore.FieldValue.serverTimestamp();

    await orderRef.set({
      id: orderRef.id,
      buyerId: buyerId,
      storeId: storeId,
      storeName: storeName,
      status: "PLACED",
      items: normalizedItems,
      itemsTotal: itemsTotal,
      shipping: shipping,
      grandTotal: grandTotal,
      createdAt: now,
      updatedAt: now,
    });

    return {ok: true, orderId: orderRef.id};
  }
);
