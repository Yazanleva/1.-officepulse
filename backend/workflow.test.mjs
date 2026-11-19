import { test, before, after } from 'node:test';
import assert from 'node:assert/strict';
import { initializeApp, deleteApp } from 'firebase-admin/app';
import { getFirestore, Timestamp } from 'firebase-admin/firestore';
import { createRequire } from 'node:module';
const require=createRequire(import.meta.url);
const {processOffice}=require('../functions/lib/process.js');
if (!process.env.FIRESTORE_EMULATOR_HOST) throw Error('Emulator required');
const app=initializeApp({projectId:'demo-officepulse-workflow'});
const db=getFirestore(app);
const make=(office,created=1,start=10,end=12)=>({officeId:office,ownerId:'alice',officeName:'Registry',topic:'Test',startAt:Timestamp.fromMillis(start*1000),endAt:Timestamp.fromMillis(end*1000),createdAt:new Timestamp(created,0),updatedAt:Timestamp.now(),status:'pending',rejectReason:''});
after(()=>deleteApp(app));
test('concurrent invocations confirm exactly one; retry has no duplicate audit',async()=>{
 const office='race-'+Date.now(); const a=db.collection('visits').doc(office+'-a');const b=db.collection('visits').doc(office+'-b');
 const batch=db.batch();batch.set(a,make(office));batch.set(b,{...make(office),ownerId:'bob'});await batch.commit();
 await Promise.all([processOffice(db,office),processOffice(db,office)]);
 assert.equal((await a.get()).get('status'),'confirmed');assert.equal((await b.get()).get('status'),'rejected');
 assert.deepEqual(await processOffice(db,office),[]);
 assert.equal((await db.collection('bookingDecisions').where('officeId','==',office).get()).size,2);
});
test('adjacent booking and rechecking edited rejected booking confirm',async()=>{
 const office='edit-'+Date.now();const a=db.collection('visits').doc(office+'-a');const b=db.collection('visits').doc(office+'-b');
 await a.set(make(office));await b.set(make(office,2,11,13));await processOffice(db,office);
 assert.equal((await b.get()).get('status'),'rejected');
 await b.update({startAt:Timestamp.fromMillis(12000),endAt:Timestamp.fromMillis(14000),status:'pending',rejectReason:''});
 await processOffice(db,office);assert.equal((await b.get()).get('status'),'confirmed');
 assert.equal((await db.collection('bookingDecisions').where('officeId','==',office).get()).size,3);
});
test('different offices do not conflict',async()=>{
 const office='independent-'+Date.now();const a=db.collection('visits').doc(office+'-a');const b=db.collection('visits').doc(office+'-b');
 await a.set(make(office));await b.set(make(office+'2'));await Promise.all([processOffice(db,office),processOffice(db,office+'2')]);
 assert.equal((await a.get()).get('status'),'confirmed');assert.equal((await b.get()).get('status'),'confirmed');
});
