import { FieldValue, Firestore } from "firebase-admin/firestore";
import { decide, Slot } from "./decide";

export async function processOffice(db: Firestore, officeId: string) {
  const lock = db.collection("bookingLocks").doc(officeId);
  return db.runTransaction(async tx => {
    // Serialize decisions for the same office, including transactions with disjoint visit IDs.
    await tx.get(lock);
    const snapshot = await tx.get(db.collection("visits").where("officeId", "==", officeId));
    const slots: Slot[] = snapshot.docs.map(doc => {
      const d = doc.data();
      return {id: doc.id, start: d.startAt.toMillis(), end: d.endAt.toMillis(),
        created: String(d.createdAt.seconds).padStart(12,"0") + String(d.createdAt.nanoseconds).padStart(9,"0"),
        status: d.status};
    });
    const decisions = decide(slots);
    if (!decisions.length) return [];
    tx.set(lock, {updatedAt: FieldValue.serverTimestamp()});
    for (const decision of decisions) {
      const ref = db.collection("visits").doc(decision.id);
      tx.update(ref, {status: decision.status, rejectReason: decision.rejectReason,
        updatedAt: FieldValue.serverTimestamp()});
      // Durable audit is atomic with the decision. Retries cannot duplicate a committed decision.
      tx.create(db.collection("bookingDecisions").doc(), {...decision, officeId,
        decidedAt: FieldValue.serverTimestamp()});
    }
    return decisions;
  });
}
