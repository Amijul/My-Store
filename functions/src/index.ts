import { onCall, HttpsError } from "firebase-functions/v2/https";
import * as admin from "firebase-admin";

admin.initializeApp();

type OrderItemPayload = {
  productId: string;
  name: string;
  imageUrl?: string;
  unitPrice: number;
  qty: number;
};

type AddressPayload = {
  fullName: string;
  phone: string;
  line1: string;
  line2?: string | null;
  city: string;
  state: string;
  pincode: string;
};

export const createOrder = onCall(async (req) => {
  const uid = req.auth?.uid;
  if (!uid) throw new HttpsError("unauthenticated", "Login required.");

  const data = (req.data ?? {}) as {
    storeId?: string;
    storeName?: string;
    items?: OrderItemPayload[];
    address?: AddressPayload;
  };

  const storeId = String(data.storeId ?? "").trim();
  const items = (data.items ?? []) as OrderItemPayload[];
  const address = data.address;

  if (!storeId) throw new HttpsError("invalid-argument", "storeId required");
  if (!address) throw new HttpsError("invalid-argument", "address required");
  if (!Array.isArray(items) || items.length === 0) {
    throw new HttpsError("invalid-argument", "items required");
  }

  for (const it of items) {
    if (!it.productId || !it.name) throw new
    HttpsError("invalid-argument", "Invalid item");

    if (!Number.isFinite(it.unitPrice) || it.unitPrice <= 0) {
      throw new HttpsError("invalid-argument", "Invalid unitPrice");
    }
    if (!Number.isInteger(it.qty) || it.qty <= 0) {
      throw new HttpsError("invalid-argument", "Invalid qty");
    }
  }

  const db = admin.firestore();

  // Read store doc server-side
  const storeRef = db.collection("stores").doc(storeId);
  const storeSnap = await storeRef.get();
  if (!storeSnap.exists) throw new HttpsError("not-found", "Store not found");

  const ownerUid = String(storeSnap.get("ownerUid") ?? "").trim();

  if (!ownerUid) throw new
  HttpsError("failed-precondition", "Store owner missing");

  const storeName =
   String(storeSnap.get("name") ?? "").trim() ||
    String(data.storeName ?? "").trim();

  if (!storeName) throw new
  HttpsError("failed-precondition", "Store name missing");

  // Buyer snapshot (optional)
  const userSnap = await db.collection("users").doc(uid).get();
  const buyerName =
    String(userSnap.get("name") ?? "").trim() ||
     String(address.fullName ?? "").trim() || "Buyer";

  const buyerPhone =
    String(userSnap.get("phone") ?? "").trim() ||
     String(address.phone ?? "").trim() || "";

  const itemsTotal = items.reduce((sum, it) => sum + it.unitPrice * it.qty, 0);
  const shipping = 0;
  const grandTotal = itemsTotal + shipping;

  // GLOBAL ORDER DOC
  const orderRef = db.collection("orders").doc();
  const orderId = orderRef.id;
  const now = admin.firestore.FieldValue.serverTimestamp();

  const orderDoc = {
    orderId,
    storeId,
    storeName,
    ownerUid,

    buyerId: uid,
    buyerName,
    buyerPhone,

    status: "PLACED",
    createdAt: now,
    updatedAt: now,

    itemsTotal,
    shipping,
    grandTotal,

    address: {
      fullName: address.fullName,
      phone: address.phone,
      line1: address.line1,
      line2: address.line2 ?? null,
      city: address.city,
      state: address.state,
      pincode: address.pincode,
    },
  };

  const batch = db.batch();
  batch.set(orderRef, orderDoc);

  for (const it of items) {
    const itemRef = orderRef.collection("items").doc();
    batch.set(itemRef, {
      itemId: itemRef.id,
      productId: it.productId,
      name: it.name,
      imageUrl: it.imageUrl ?? "",
      unitPrice: it.unitPrice,
      qty: it.qty,
      lineTotal: it.unitPrice * it.qty,
    });
  }

  await batch.commit();
  return { orderId };
});
