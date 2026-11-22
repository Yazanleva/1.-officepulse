export interface Slot {
  id: string;
  start: number;
  end: number;
  created: string;
  status: string;
}
export interface Decision { id: string; status: "confirmed" | "rejected"; rejectReason: string }
export const overlaps = (a: Slot, b: Slot) => a.start < b.end && b.start < a.end;
// Confirmed reservations keep their place; pending requests use server creation time and ID.
export function decide(slots: Slot[]): Decision[] {
  const accepted = slots.filter(s => s.status === "confirmed");
  const pending = slots.filter(s => s.status === "pending").sort((a,b) =>
    a.created < b.created ? -1 : a.created > b.created ? 1 : a.id < b.id ? -1 : a.id > b.id ? 1 : 0);
  return pending.map(slot => {
    const conflict = accepted.some(other => overlaps(slot, other));
    if (!conflict) accepted.push(slot);
    return {id: slot.id, status: conflict ? "rejected" : "confirmed",
      rejectReason: conflict ? "This office is already booked during that time. Choose another time." : ""};
  });
}
