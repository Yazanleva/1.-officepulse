import { initializeApp } from 'firebase-admin/app';
import { getAuth } from 'firebase-admin/auth';
import { getFirestore } from 'firebase-admin/firestore';
import { randomBytes } from 'node:crypto';

process.env.FIRESTORE_EMULATOR_HOST = '127.0.0.1:8080';
process.env.FIREBASE_AUTH_EMULATOR_HOST = '127.0.0.1:9099';
initializeApp({ projectId: 'demo-officepulse' });
const offices = [
  ['registry', 'Student Registry', 'Main building', 15],
  ['exchange', 'International Office', 'West building', 30],
  ['finance', 'Student Finance', 'North building', 20]
];
for (const [id, name, building, serviceMinutes] of offices) {
  await getFirestore().collection('offices').doc(id).set({ name, building, serviceMinutes });
}
for (const id of ['student-a', 'student-b']) {
  const email = id + '@officepulse.example';
  const password = randomBytes(12).toString('hex');
  try { await getAuth().createUser({uid:id,email,password}); }
  catch (error) {
    if (error.code !== 'auth/uid-already-exists' && error.code !== 'auth/email-already-exists') throw error;
    await getAuth().updateUser(id,{password});
  }
  console.log('Local emulator login:', email, password);
}

