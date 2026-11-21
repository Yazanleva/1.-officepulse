import { before, after, test } from 'node:test';
import { readFile } from 'node:fs/promises';
import { initializeTestEnvironment, assertSucceeds, assertFails } from '@firebase/rules-unit-testing';
import { doc, getDoc, setDoc, updateDoc, deleteDoc, serverTimestamp, Timestamp } from 'firebase/firestore';

let env, alice, bob, guest;
const visit = (db,id='original') => doc(db,'visits',id);
const office = db => doc(db,'offices','registry');
const fields = () => ({
  ownerId:'alice',officeId:'registry',officeName:'Registry',topic:'Registration',
  startAt:Timestamp.fromMillis(1000),endAt:Timestamp.fromMillis(2000),
  createdAt:serverTimestamp(),updatedAt:serverTimestamp(),status:'pending',rejectReason:''
});
before(async () => {
  env = await initializeTestEnvironment({projectId:'demo-officepulse', firestore:{
    rules:await readFile(new URL('../firestore.rules',import.meta.url),'utf8')}});
  alice=env.authenticatedContext('alice').firestore();
  bob=env.authenticatedContext('bob').firestore();
  guest=env.unauthenticatedContext().firestore();
  await env.withSecurityRulesDisabled(async ctx => {
    const db=ctx.firestore();
    await setDoc(office(db),{name:'Registry'});
    await setDoc(visit(db,'confirmed'),{...fields(),status:'confirmed'});
  });
  await assertSucceeds(setDoc(visit(alice),fields()));
});
after(async () => { await env?.cleanup(); });
test('1 owner reads visit',() => assertSucceeds(getDoc(visit(alice))));
test('2 other student cannot read visit',() => assertFails(getDoc(visit(bob))));
test('3 signed-out read denied',() => assertFails(getDoc(visit(guest))));
test('4 owner comes from session',() => assertFails(setDoc(visit(bob,'foreign'),fields())));
test('5 end must follow start',() => assertFails(setDoc(visit(alice,'invalid'),{...fields(),endAt:Timestamp.fromMillis(999)})));
test('6 owner is immutable',() => assertFails(updateDoc(visit(alice),{ownerId:'bob',updatedAt:serverTimestamp()})));
test('7 offices are read-only',() => assertFails(setDoc(office(alice),{name:'New office'})));
test('8 students can read offices',() => assertSucceeds(getDoc(office(alice))));
test('9 client cannot create confirmed visit',() => assertFails(setDoc(visit(alice,'forged'),{...fields(),status:'confirmed'})));
test('10 client cannot confirm pending visit',() => assertFails(updateDoc(visit(alice),{status:'confirmed',updatedAt:serverTimestamp()})));
test('11 changing a confirmed slot requires pending',() => assertFails(updateDoc(visit(alice,'confirmed'),{endAt:Timestamp.fromMillis(3000),updatedAt:serverTimestamp()})));


test('12 client cannot create rejected visit',() => assertFails(setDoc(visit(alice,'forged-reject'),{...fields(),status:'rejected'})));
test('13 unknown status denied',() => assertFails(setDoc(visit(alice,'unknown'),{...fields(),status:'approved'})));
test('14 client cannot forge rejection reason',() => assertFails(updateDoc(visit(alice),{rejectReason:'fake',updatedAt:serverTimestamp()})));
test('15 created timestamp cannot change',() => assertFails(updateDoc(visit(alice),{createdAt:Timestamp.fromMillis(1),updatedAt:serverTimestamp()})));
test('16 other user cannot delete',() => assertFails(deleteDoc(visit(bob))));
test('17 extra field denied',() => assertFails(setDoc(visit(alice,'extra'),{...fields(),admin:true})));
test('18 owner can edit slot when returning to pending',() => assertSucceeds(updateDoc(visit(alice,'confirmed'),{startAt:Timestamp.fromMillis(1100),status:'pending',rejectReason:'',updatedAt:serverTimestamp()})));
test('19 owner can delete own visit',async()=>{await setDoc(visit(alice,'delete'),fields());await assertSucceeds(deleteDoc(visit(alice,'delete')))});
test('20 client cannot read or write server audit',async()=>{await assertFails(getDoc(doc(alice,'bookingDecisions','one')));await assertFails(setDoc(doc(alice,'bookingLocks','registry'),{updatedAt:serverTimestamp()}))});
