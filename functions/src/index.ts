import { initializeApp } from "firebase-admin/app";
import { getFirestore } from "firebase-admin/firestore";
import { onDocumentWritten } from "firebase-functions/v2/firestore";
import { logger } from "firebase-functions";
import { processOffice } from "./process";
initializeApp();
export const checkVisit = onDocumentWritten("visits/{id}", async event => {
  const data = event.data?.after.data();
  if (!data || data.status !== "pending") return;
  const decisions = await processOffice(getFirestore(), data.officeId);
  for (const decision of decisions) logger.info("Booking decision", decision);
});
