package com.example.fitnessrun

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.MemoryCacheSettings
import com.google.firebase.firestore.Source

data class CloudProfile(val uid: String, val fields: Map<String, String>)
class MissingProfileException : Exception()
class ExistingProfileException : Exception()
class AccountTypeException : Exception()

object CloudProfiles {
    val auth: FirebaseAuth get() = FirebaseAuth.getInstance()
    private val database: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance().apply {
            firestoreSettings = FirebaseFirestoreSettings.Builder()
                .setLocalCacheSettings(MemoryCacheSettings.newBuilder().build()).build()
        }
    }

    fun signIn(email: String, password: String): Task<CloudProfile> =
        auth.signInWithEmailAndPassword(email, password).onSuccessTask { load() }

    fun signIn(email: String, password: String, accountType: String): Task<CloudProfile> =
        signIn(email, password).continueWith { result ->
            val profile = result.getResult(Exception::class.java)
            if (!ProfileIdentity.matches(profile.fields, accountType)) {
                auth.signOut()
                throw AccountTypeException()
            }
            profile
        }

    fun load(): Task<CloudProfile> {
        val uid = auth.currentUser?.uid ?: return Tasks.forException(IllegalStateException())
        return CloudReads.bounded(database.collection("users").document(uid).get(Source.SERVER)).continueWith { result ->
            val document = result.getResult(Exception::class.java)
            if (auth.currentUser?.uid != uid) throw IllegalStateException()
            if (!document.exists()) throw MissingProfileException()
            CloudProfile(uid, ProfileFields.sanitize(document.get("profile") as? Map<*, *> ?: emptyMap<String, String>()))
        }
    }

    fun register(fields: Map<String, String>, password: String): Task<CloudProfile> {
        val profile = ProfileFields.sanitize(fields)
        val email = profile[RegistrationKeys.EMAIL].orEmpty()
        // If Auth succeeded but Firestore failed earlier, the same credentials can finish
        // the missing profile. An existing profile is never overwritten by registration.
        return auth.createUserWithEmailAndPassword(email, password).continueWithTask { result ->
            if (result.isSuccessful) Tasks.forResult(result.result)
            else if (result.exception is FirebaseAuthUserCollisionException) auth.signInWithEmailAndPassword(email, password)
            else Tasks.forException(result.exception ?: IllegalStateException())
        }.onSuccessTask { account ->
            val uid = account.user!!.uid
            val normalized = profile + (RegistrationKeys.EMAIL to account.user!!.email.orEmpty()) +
                (ProfileIdentity.TYPE to ProfileIdentity.type(profile))
            val reference = database.collection("users").document(uid)
            database.runTransaction { transaction ->
                if (transaction.get(reference).exists()) throw ExistingProfileException()
                transaction.set(reference, mapOf("profile" to normalized, "updatedAt" to FieldValue.serverTimestamp()))
                CloudProfile(uid, normalized)
            }
        }
    }

    fun updateIdentity(uid: String, changes: Map<String, String>): Task<CloudProfile> {
        if (auth.currentUser?.uid != uid) return Tasks.forException(IllegalStateException())
        val reference = database.collection("users").document(uid)
        return database.runTransaction { transaction ->
            val document = transaction.get(reference)
            if (!document.exists()) throw MissingProfileException()
            val original = ProfileFields.sanitize(document.get("profile") as? Map<*, *> ?: emptyMap<String, String>())
            val updated = ProfileFields.editIdentity(original, changes)
            transaction.set(reference, mapOf("profile" to updated, "updatedAt" to FieldValue.serverTimestamp()))
            CloudProfile(uid, updated)
        }
    }
}
