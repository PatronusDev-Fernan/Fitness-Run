package com.example.fitnessrun

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CloudProgress {
    private val database get() = FirebaseFirestore.getInstance()
    fun today(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    fun loadToday(uid: String): Task<ProgressSnapshot> {
        if (CloudProfiles.auth.currentUser?.uid != uid) return Tasks.forException(IllegalStateException())
        val date = today()
        return database.collection("users").document(uid).collection("dailyProgress")
            .whereGreaterThanOrEqualTo("date", ProgressSnapshot.weekStart(date))
            .whereLessThanOrEqualTo("date", date)
            .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(1).get(Source.SERVER).continueWith { result ->
            check(CloudProfiles.auth.currentUser?.uid == uid)
            val data = result.getResult(Exception::class.java).documents.firstOrNull()?.data.orEmpty()
            ProgressSnapshot(
                data["date"] as? String ?: date,
                intSet(data["completedRoutines"]),
                intSet(data["completedMeals"]),
                checks(data["exerciseChecks"]),
                data["weekendRoutine"] as? String ?: "Sin seleccionar",
                data["weekendFood"] as? String ?: "Sin seleccionar"
            ).validate().forDate(date)
        }
    }

    fun save(uid: String, snapshot: ProgressSnapshot): Task<Void> {
        if (CloudProfiles.auth.currentUser?.uid != uid) return Tasks.forException(IllegalStateException())
        try { snapshot.validate() } catch (error: IllegalArgumentException) { return Tasks.forException(error) }
        val data = mapOf(
            "date" to snapshot.date,
            "completedRoutines" to snapshot.completedRoutines.sorted(),
            "completedMeals" to snapshot.completedMeals.sorted(),
            "exerciseChecks" to snapshot.exerciseChecks.mapKeys { it.key.toString() }.mapValues { it.value.sorted() },
            "weekendRoutine" to snapshot.weekendRoutine,
            "weekendFood" to snapshot.weekendFood,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        return document(uid, snapshot.date).set(data)
    }

    fun history(uid: String, limit: Long = 30): Task<com.google.firebase.firestore.QuerySnapshot> {
        if (CloudProfiles.auth.currentUser?.uid != uid) return Tasks.forException(IllegalStateException())
        return CloudReads.bounded(database.collection("users").document(uid).collection("dailyProgress")
            .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING).limit(limit).get(Source.SERVER))
    }

    private fun document(uid: String, date: String) =
        database.collection("users").document(uid).collection("dailyProgress").document(date)

    private fun intSet(value: Any?): Set<Int> = (value as? List<*>)
        ?.mapNotNull { (it as? Number)?.toInt() }?.toSet().orEmpty()

    private fun checks(value: Any?): Map<Int, Set<Int>> = (value as? Map<*, *>)?.mapNotNull { (key, item) ->
        key.toString().toIntOrNull()?.let { it to intSet(item) }
    }?.toMap().orEmpty()
}
