import { before, after, beforeEach, test } from 'node:test';
import { readFileSync } from 'node:fs';
import { initializeTestEnvironment, assertFails, assertSucceeds } from '@firebase/rules-unit-testing';
import { doc, collection, getDoc, getDocs, setDoc, updateDoc, serverTimestamp } from 'firebase/firestore';

let env;
before(async () => {
  if (!process.env.FIRESTORE_EMULATOR_HOST) throw new Error('Only run against the emulator.');
  env = await initializeTestEnvironment({ projectId: 'demo-fitnessrun', firestore: { rules: readFileSync('firestore.rules', 'utf8') } });
});
after(async () => { await env?.cleanup(); });
beforeEach(async () => { await env.clearFirestore(); });
const account = uid => env.authenticatedContext(uid, { email: `${uid}@example.test` }).firestore();
const profile = (uid, role = 'user') => ({ profile: {
  'registration.name': 'Prueba', 'registration.last_name': 'Cuenta',
  'registration.birth_date': '1990-01-01', 'registration.email': `${uid}@example.test`,
  'registration.account_type': role, 'registration.bio': 'Mi descripción'
}, updatedAt: serverTimestamp() });
const progress = () => ({ date: '2026-10-06', completedRoutines: [0], completedMeals: [0, 2],
  exerciseChecks: { '0': [0, 1, 2] }, weekendRoutine: 'Descanso',
  weekendFood: 'Sin seleccionar', updatedAt: serverTimestamp() });
const path = uid => `users/${uid}/dailyProgress/2026-10-06`;

test('user and coach save and retrieve their own profiles', async () => {
  for (const [uid, role] of [['user-a', 'user'], ['coach-b', 'coach']]) {
    await assertSucceeds(setDoc(doc(account(uid), `users/${uid}`), profile(uid, role)));
    await assertSucceeds(getDoc(doc(account(uid), `users/${uid}`)));
  }
});
test('coach label grants no access to another profile or progress', async () => {
  const coach = account('coach-b');
  await assertSucceeds(setDoc(doc(coach, 'users/coach-b'), profile('coach-b', 'coach')));
  await assertFails(getDoc(doc(coach, 'users/user-a')));
  await assertFails(getDoc(doc(coach, path('user-a'))));
  await assertFails(setDoc(doc(coach, path('user-a')), progress()));
  await assertFails(getDocs(collection(coach, 'users')));
});
test('anonymous clients cannot read or write profiles and progress', async () => {
  const db = env.unauthenticatedContext().firestore();
  await assertFails(getDoc(doc(db, 'users/user-a')));
  await assertFails(setDoc(doc(db, 'users/user-a'), profile('user-a')));
  await assertFails(getDoc(doc(db, path('user-a'))));
  await assertFails(setDoc(doc(db, path('user-a')), progress()));
});
test('owner persists progress and can query only their own history', async () => {
  const db = account('user-a');
  await assertSucceeds(setDoc(doc(db, path('user-a')), progress()));
  await assertSucceeds(getDoc(doc(db, path('user-a'))));
  await assertSucceeds(getDocs(collection(db, 'users/user-a/dailyProgress')));
  await assertFails(getDocs(collection(db, 'users/coach-b/dailyProgress')));
});
test('rejects invalid progress, extra fields and forged timestamps', async () => {
  const ref = doc(account('user-a'), path('user-a'));
  for (const patch of [
    { completedMeals: [4] }, { completedMeals: [0, 0] },
    { completedRoutines: [0], exerciseChecks: { '0': [0] } },
    { exerciseChecks: { '9': [0] } }, { date: '2026-10-07' },
    { weekendFood: 'unknown' }, { admin: true }, { updatedAt: new Date(0) }
  ]) {
    await env.clearFirestore();
    await assertFails(setDoc(ref, { ...progress(), ...patch }));
    await assertSucceeds(setDoc(ref, progress()));
    await assertFails(updateDoc(ref, { ...patch, updatedAt: patch.updatedAt ?? serverTimestamp() }));
  }
});
test('owner cannot change account role or impersonate an email', async () => {
  const ref = doc(account('user-a'), 'users/user-a');
  await assertSucceeds(setDoc(ref, profile('user-a')));
  await assertFails(setDoc(ref, profile('user-a', 'coach')));
  await assertFails(setDoc(ref, profile('coach-b')));
});
