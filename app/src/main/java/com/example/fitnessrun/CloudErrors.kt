package com.example.fitnessrun

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestoreException

object CloudErrors {
    fun message(error: Exception): Int {
        val causes = generateSequence<Throwable>(error) { it.cause }.take(8).toList()
        if (causes.any { it is java.util.concurrent.TimeoutException }) return R.string.cloud_read_timeout
        if (causes.any { it is AccountTypeException }) return R.string.account_type_mismatch
        if (causes.any { it is MissingProfileException }) return R.string.cloud_profile_missing
        if (causes.any { it is ExistingProfileException }) return R.string.cloud_profile_exists
        if (causes.any { it is FirebaseNetworkException }) return R.string.cloud_network_error
        val firestore = causes.filterIsInstance<FirebaseFirestoreException>().firstOrNull()
        if (firestore != null) return when (firestore.code) {
            FirebaseFirestoreException.Code.PERMISSION_DENIED -> R.string.cloud_rules_error
            FirebaseFirestoreException.Code.UNAVAILABLE, FirebaseFirestoreException.Code.DEADLINE_EXCEEDED -> R.string.cloud_network_error
            FirebaseFirestoreException.Code.NOT_FOUND, FirebaseFirestoreException.Code.FAILED_PRECONDITION -> R.string.cloud_setup_error
            else -> R.string.cloud_generic_error
        }
        return when (causes.filterIsInstance<FirebaseAuthException>().firstOrNull()?.errorCode) {
            "ERROR_OPERATION_NOT_ALLOWED", "ERROR_CONFIGURATION_NOT_FOUND" -> R.string.cloud_auth_disabled
            "ERROR_WEAK_PASSWORD" -> R.string.cloud_weak_password
            "ERROR_TOO_MANY_REQUESTS" -> R.string.cloud_too_many_requests
            "ERROR_INVALID_EMAIL", "ERROR_WRONG_PASSWORD", "ERROR_USER_NOT_FOUND", "ERROR_INVALID_CREDENTIAL", "ERROR_INVALID_LOGIN_CREDENTIALS" -> R.string.cloud_invalid_login
            else -> R.string.cloud_generic_error
        }
    }
}
