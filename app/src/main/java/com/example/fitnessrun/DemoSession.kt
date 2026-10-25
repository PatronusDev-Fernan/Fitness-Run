package com.example.fitnessrun

import android.content.Intent
import android.content.Context
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks

/** Session state. Only authenticated progress and profiles are persisted. */
object DemoSession {
    private var generation = 0
    private var loadedDate: String? = null
    private var loading: Task<ProgressSnapshot>? = null
    private var saving: Task<Void>? = null
    var progressError: String? = null
        private set
    fun canEditProgress(context: Context): Boolean {
        if (!isCloud || loadedDate == CloudProgress.today()) return true
        Toast.makeText(context.applicationContext, "Vuelve a abrir esta pantalla para cargar el progreso de hoy.", Toast.LENGTH_LONG).show()
        return false
    }
    const val CLOUD_UID = "session.cloud_uid"
    var cloudUid: String? = null
        private set
    val isCloud get() = cloudUid != null
    var active = false
        private set
    val profile = mutableMapOf<String, String>()
    var progress = DemoProgress()
        private set
    val posts = mutableListOf<Post>()
    var following = false
    var supported = false
    var congratulated = false
    var saved = false
    var followingCoach = false
    var weekendRoutine = "Sin seleccionar"
    var weekendFood = "Sin seleccionar"

    data class Post(val text: String, val audience: String, val media: String?, val mediaType: String, val progress: String? = null)

    fun clear() {
        generation++
        loadedDate = null
        loading = null
        saving = null
        progressError = null
        cloudUid = null
        active = false
        profile.clear()
        progress = DemoProgress()
        posts.clear()
        following = false
        supported = false
        congratulated = false
        saved = false
        followingCoach = false
        weekendRoutine = "Sin seleccionar"
        weekendFood = "Sin seleccionar"
    }

    fun begin(source: Intent) {
        clear()
        val extras = RegistrationKeys.copyTo(source, Intent()).extras
        extras?.keySet()?.forEach { key -> extras.getString(key)?.let { profile[key] = it } }
        active = true
        cloudUid = source.getStringExtra(CLOUD_UID)
    }

    fun beginCloud(profile: CloudProfile) {
        begin(Intent().apply { profile.fields.forEach { (key, value) -> putExtra(key, value) } })
        cloudUid = profile.uid
    }

    fun loadToday(onComplete: (() -> Unit)? = null) {
        val uid = cloudUid ?: run { onComplete?.invoke(); return }
        if (loadedDate == CloudProgress.today()) { onComplete?.invoke(); return }
        val session = generation
        val request = loading ?: CloudProgress.loadToday(uid).also { task ->
            loading = task
            task.addOnCompleteListener { result ->
                if (session != generation) return@addOnCompleteListener
                loading = null
                if (result.isSuccessful) {
                    val snapshot = result.result
                    progress.restore(snapshot)
                    weekendRoutine = snapshot.weekendRoutine
                    weekendFood = snapshot.weekendFood
                    loadedDate = snapshot.date
                    progressError = null
                } else {
                    progressError = "No se pudo cargar tu progreso. Revisa la conexión y vuelve a abrir esta pantalla."
                }
            }
        }
        request.addOnCompleteListener { if (session == generation) onComplete?.invoke() }
    }

    fun saveToday(context: Context, notifySuccess: Boolean = false) {
        val uid = cloudUid ?: return
        val app = context.applicationContext
        if (loadedDate != CloudProgress.today()) {
            Toast.makeText(app, "Abre nuevamente esta pantalla para cargar el progreso antes de guardarlo.", Toast.LENGTH_LONG).show()
            return
        }
        val session = generation
        val snapshot = ProgressSnapshot(
            loadedDate!!, progress.completedRoutines.toSet(), progress.completedMeals.toSet(),
            progress.exerciseChecks.mapValues { it.value.toSet() }, weekendRoutine, weekendFood
        )
        val previous = saving ?: Tasks.forResult<Void>(null)
        saving = previous.continueWithTask {
            if (session != generation) Tasks.forException(IllegalStateException())
            else CloudProgress.save(uid, snapshot)
        }.addOnCompleteListener { result ->
            if (session != generation) return@addOnCompleteListener
            if (!result.isSuccessful) Toast.makeText(app,
                "No se guardó el progreso en Firebase. Conservamos los cambios en esta sesión; pulsa Guardar para reintentar.", Toast.LENGTH_LONG).show()
            else if (notifySuccess) Toast.makeText(app, "Progreso guardado en Firebase", Toast.LENGTH_SHORT).show()
        }
    }

    fun beginExample() = begin(Intent().apply {
        putExtra(RegistrationKeys.NAME, "Invitado")
        putExtra(RegistrationKeys.DIET_TYPE, "Ninguna en particular")
        putExtra(RegistrationKeys.FOOD_GOAL, "Mejorar hábitos")
    })
    fun fill(destination: Intent): Intent = destination.apply {
        profile.forEach { (key, value) -> putExtra(key, value) }
        if (cloudUid != null) putExtra(CLOUD_UID, cloudUid) else removeExtra(CLOUD_UID)
    }

    val displayName get() = fullName + if (ProfileIdentity.type(profile) == ProfileIdentity.COACH) " @coach" else ""
    val name get() = profile[RegistrationKeys.NAME].orEmpty()
    val fullName get() = listOf(profile[RegistrationKeys.NAME], profile[RegistrationKeys.LAST_NAME])
        .filterNotNull().filter { it.isNotBlank() }.joinToString(" ")
}


