package com.example.fitnessrun

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.textfield.TextInputEditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.fitnessrun.databinding.ActivitySectionBinding

class
SectionActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySectionBinding
    private lateinit var section: Section
    private val requests by lazy { ViewModelProvider(this)[ProfileRequestViewModel::class.java] }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!DemoSession.active) DemoSession.begin(intent)
        enableEdgeToEdge()
        binding = ActivitySectionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.sectionRoot) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
        section = Section.from(intent.getStringExtra(EXTRA_SECTION))
        render(section)
        if (section == Section.PROFILE) renderProfile()
        configureNavigation()
        DemoSession.loadToday {
            runOnUiThread {
                if (isFinishing || isDestroyed) return@runOnUiThread
                DemoSession.progressError?.let { android.widget.Toast.makeText(this, it, android.widget.Toast.LENGTH_LONG).show() }
                if (section == Section.PROFILE) renderProfile() else renderDemoProgress()
            }
        }
        binding.exitDemoButton.setText(if (DemoSession.isCloud) R.string.cloud_logout else R.string.demo_logout)
        binding.exitDemoButton.setOnClickListener {
            AlertDialog.Builder(this).setTitle(if (DemoSession.isCloud) R.string.cloud_logout else R.string.demo_logout)
                .setMessage(if (DemoSession.isCloud) R.string.cloud_logout_message else R.string.demo_logout_message)
                .setNegativeButton(R.string.dialog_cancel, null)
                .setPositiveButton(if (DemoSession.isCloud) R.string.cloud_logout else R.string.demo_logout) { _, _ ->
                    if (com.google.firebase.FirebaseApp.getApps(this).isNotEmpty()) CloudProfiles.auth.signOut()
                    RegistrationDraft.clear()
                    DemoSession.clear()
                    startActivity(Intent(this, LoginActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
                }.show()
        }
    }

    override fun onResume() {
        super.onResume()
        DemoSession.fill(intent)
        if (section == Section.PROFILE) renderProfile() else renderDemoProgress()
    }

    private fun renderDemoProgress() {
        val progress = DemoSession.progress
        val routineCount = progress.completedRoutines.size
        when (section) {
            Section.ROUTINE -> {
                binding.subtitleText.text = "De lunes a viernes asignamos tu plan según los datos del registro. Tú eliges solamente el fin de semana."
                binding.summaryTitleText.setText(R.string.nav_routine)
                binding.summaryDetailText.text = getString(R.string.demo_routine_progress, routineCount)
                binding.summaryProgress.progress = progress.routinePercent
                binding.summaryButton.setText(R.string.demo_open)
                val plan = PersonalizationEngine.weekdayRoutines()
                binding.cardOneTitle.text = "Lunes a miércoles · Plan asignado"
                binding.cardOneDetail.text = plan.take(3).joinToString("\n") { "${it.day}: ${it.title} · ${it.detail}" }
                binding.cardTwoTitle.text = "Jueves y viernes · Plan asignado"
                binding.cardTwoDetail.text = plan.drop(3).joinToString("\n") { "${it.day}: ${it.title} · ${it.detail}" }
                binding.cardThreeTitle.text = "Fin de semana · Tú eliges"
                binding.cardThreeDetail.text = "Selección actual: ${DemoSession.weekendRoutine}"
                binding.cardOneButton.text = "Ver plan"
                binding.cardTwoButton.text = "Ver plan"
                binding.cardThreeButton.text = "Elegir actividad"
                binding.cardOneButton.setOnClickListener { openDetail(1) }
                binding.cardTwoButton.setOnClickListener { openDetail(2) }
                binding.cardThreeButton.setOnClickListener { chooseWeekendRoutine() }
            }
            Section.FOOD -> {
                binding.subtitleText.text = "El menú es orientativo. Las restricciones registradas requieren revisión antes de elegir alimentos."
                binding.summaryTitleText.setText(R.string.nav_food)
                binding.summaryDetailText.text = getString(R.string.demo_meal_progress, progress.completedMeals.size)
                binding.summaryProgress.progress = progress.mealPercent
                binding.summaryButton.setText(R.string.demo_mark_meals)
                val meals = PersonalizationEngine.safeMeals()
                binding.cardOneTitle.text = "Lunes a miércoles · Menú asignado"
                binding.cardOneDetail.text = meals.joinToString("\n") { "${it.day}: ${it.title}" }
                binding.cardTwoTitle.text = "Restricciones registradas"
                binding.cardTwoDetail.text = PersonalizationEngine.exclusionsLabel()
                binding.cardThreeTitle.text = "Fin de semana · Tú eliges"
                binding.cardThreeDetail.text = "Selección actual: ${DemoSession.weekendFood}"
                binding.cardOneButton.text = "Ver menú"
                binding.cardTwoButton.text = "Revisar registro"
                binding.cardThreeButton.text = "Elegir opción"
                binding.cardOneButton.setOnClickListener { openDetail(1) }
                binding.cardTwoButton.setOnClickListener { startActivity(intentFor(Section.PROFILE)); finish() }
                binding.cardThreeButton.setOnClickListener { chooseWeekendFood() }
            }
            Section.ACHIEVEMENTS -> {
                binding.subtitleText.setText(R.string.demo_achievements_hint)
                binding.summaryTitleText.setText(R.string.demo_achievements_title)
                binding.summaryDetailText.text = getString(R.string.demo_routine_progress, routineCount)
                binding.summaryProgress.progress = progress.routinePercent
                binding.summaryButton.setText(R.string.demo_open)
                binding.cardOneTitle.setText(R.string.demo_first_step)
                binding.cardOneDetail.setText(R.string.demo_first_step_hint)
                binding.cardTwoTitle.setText(R.string.demo_consistency)
                binding.cardTwoDetail.setText(R.string.demo_consistency_hint)
                binding.cardThreeTitle.setText(R.string.demo_food_badge)
                binding.cardThreeDetail.setText(R.string.demo_food_badge_hint)
                binding.cardOneButton.setText(if (routineCount > 0) R.string.demo_completed else R.string.demo_pending)
                binding.cardTwoButton.setText(if (routineCount == DemoProgress.ROUTINE_COUNT) R.string.demo_completed else R.string.demo_pending)
                binding.cardThreeButton.setText(if (progress.completedMeals.size == 4) R.string.demo_completed else R.string.demo_pending)
            }
            else -> Unit
        }
    }

    private fun editProfile() {
        startActivity(DemoSession.fill(Intent(this, ProfileEditorActivity::class.java)))
    }
    private fun chooseWeekendRoutine() {
        if (!DemoSession.canEditProgress(this)) return
        val options = arrayOf("Descanso", "Caminata", "Estiramiento", "Actividad libre")
        AlertDialog.Builder(this).setTitle("Actividad de fin de semana").setSingleChoiceItems(options,
            options.indexOf(DemoSession.weekendRoutine)) { dialog, index ->
            DemoSession.weekendRoutine = options[index]
            DemoSession.saveToday(this@SectionActivity)
            binding.cardThreeDetail.text = "Selección actual: ${options[index]}"
            dialog.dismiss()
        }.setNegativeButton(R.string.dialog_cancel, null).show()
    }

    private fun chooseWeekendFood() {
        if (!DemoSession.canEditProgress(this)) return
        val options = arrayOf("Mantener el menú semanal", "Comida flexible planificada", "Elegir alternativas seguras")
        AlertDialog.Builder(this).setTitle("Alimentación de fin de semana").setSingleChoiceItems(options,
            options.indexOf(DemoSession.weekendFood)) { dialog, index ->
            DemoSession.weekendFood = options[index]
            DemoSession.saveToday(this@SectionActivity)
            binding.cardThreeDetail.text = "Selección actual: ${options[index]}"
            dialog.dismiss()
        }.setMessage(PersonalizationEngine.exclusionsLabel())
            .setNegativeButton(R.string.dialog_cancel, null).show()
    }

    private fun renderProfile() {
        binding.profileIdentityPanel.visibility = android.view.View.VISIBLE
        binding.profileBadge.text = ProfileIdentity.badge(DemoSession.profile)
        binding.profileBio.text = DemoSession.profile[ProfileIdentity.BIO]?.takeIf { it.isNotBlank() }
            ?: getString(R.string.profile_bio_empty)
        AvatarPhoto.show(binding.profileAvatar, DemoSession.profile[ProfileIdentity.PHOTO])
        fun value(key: String) = intent.getStringExtra(key)?.takeIf { it.isNotBlank() }
            ?: getString(R.string.summary_not_provided)
        fun line(resource: Int, key: String) = getString(resource, value(key))
        val fullName = listOfNotNull(intent.getStringExtra(RegistrationKeys.NAME),
            intent.getStringExtra(RegistrationKeys.LAST_NAME)).filter { it.isNotBlank() }.joinToString(" ")
        binding.subtitleText.setText(if (DemoSession.isCloud) R.string.cloud_profile_notice else R.string.profile_local_notice)
        binding.summaryTitleText.text = fullName.ifBlank { getString(R.string.profile_empty_name) }
        binding.summaryDetailText.text = line(R.string.summary_goal, RegistrationKeys.FOOD_GOAL)
        binding.summaryProgress.visibility = android.view.View.GONE
        binding.summaryButton.visibility = android.view.View.VISIBLE
        binding.summaryButton.setText(R.string.demo_edit_profile)
        binding.summaryButton.setOnClickListener { editProfile() }
        binding.listTitleText.setText(R.string.profile_registered_data)
        binding.cardOneTitle.setText(R.string.summary_personal)
        binding.cardOneDetail.text = listOf(
            getString(R.string.summary_name, fullName.ifBlank { getString(R.string.summary_not_provided) }),
            line(R.string.summary_birth_date, RegistrationKeys.BIRTH_DATE),
            line(R.string.summary_email, RegistrationKeys.EMAIL)).joinToString("\n")
        binding.cardTwoTitle.setText(R.string.summary_health)
        binding.cardTwoDetail.text = listOf(
            getString(R.string.summary_measurements, value(RegistrationKeys.HEIGHT), value(RegistrationKeys.WEIGHT)),
            line(R.string.summary_blood, RegistrationKeys.BLOOD_TYPE),
            line(R.string.summary_condition, RegistrationKeys.CONDITION),
            line(R.string.summary_injury, RegistrationKeys.INJURY),
            line(R.string.summary_allergy, RegistrationKeys.ALLERGY),
            line(R.string.summary_pain, RegistrationKeys.PAIN)).joinToString("\n")
        binding.cardThreeTitle.setText(R.string.summary_food)
        binding.cardThreeDetail.text = listOf(
            line(R.string.summary_vegetables, RegistrationKeys.VEGETABLES),
            line(R.string.summary_fruits, RegistrationKeys.FRUITS),
            line(R.string.summary_favorites, RegistrationKeys.FAVORITES),
            line(R.string.summary_dislikes, RegistrationKeys.DISLIKES),
            line(R.string.summary_restrictions, RegistrationKeys.RESTRICTIONS),
            line(R.string.summary_diet, RegistrationKeys.DIET_TYPE)).joinToString("\n")
        listOf(binding.cardOneButton, binding.cardTwoButton, binding.cardThreeButton).forEach {
            it.visibility = android.view.View.GONE
        }
    }

    private fun render(value: Section) {
        val content = when (value) {
            Section.ROUTINE -> SectionContent(
                "TU PLAN", "Rutina semanal", "Entrena a tu ritmo y registra cada avance.",
                "Entrenamiento de hoy", "Pierna y movilidad · Principiante · 30 min", 40, "Continuar rutina", "Esta semana",
                Card("Lunes · Pierna y movilidad", "2 de 5 ejercicios completados · 20 de 30 min", "Continuar"),
                Card("Martes · Tren superior", "5 ejercicios · 35 min · Pendiente", "Ver rutina"),
                Card("Fin de semana flexible", "Sábado: actividad opcional · Domingo: recuperación", "Agregar actividad")
            )
            Section.FOOD -> SectionContent(
                "ALIMENTACIÓN", "Plan de alimentación", "Opciones equilibradas basadas en tus preferencias.",
                "Siguiente comida", "Comida · 2:30 p. m. · Pollo, arroz y verduras", 65, "Ver plan de hoy", "Tu día",
                Card("Desayuno", "Avena con plátano · Yogur natural · Agua", "Ver alimentos"),
                Card("Comida", "Proteína, cereal y verduras · Puedes intercambiar opciones", "Ver alternativas"),
                Card("Sábado y domingo", "Plan flexible con recomendaciones de hidratación y porciones", "Planificar")
            )
            Section.ACHIEVEMENTS -> SectionContent(
                "TU PROGRESO", "Logros", "La constancia desbloquea contenido para seguir avanzando.",
                "Nivel 2 · En movimiento", "Completa 2 entrenamientos más para desbloquear un video", 72, "Ver recompensa", "Insignias recientes",
                Card("Primer paso", "Completaste tu primera rutina", "Conseguido ✓"),
                Card("Constancia", "Entrena 3 días durante una semana · 2 de 3", "Ver progreso"),
                Card("Próximo desbloqueo", "Video: movilidad para después de entrenar", "Vista previa")
            )
            Section.PROFILE -> SectionContent(
                "TU CUENTA", "Perfil", "Administra tus datos, objetivo y privacidad.",
                "Fernando", "Objetivo: mejorar condición física · Perfil de alumno", 80, "Editar perfil", "Configuración",
                Card("Datos personales", "Nombre, fecha de nacimiento, correo y contraseña", "Administrar"),
                Card("Salud y preferencias", "Condiciones, molestias, alergias y alimentación", "Actualizar"),
                Card("Privacidad y seguridad", "Controla quién ve tus publicaciones y datos. Tu información médica es privada.", "Configurar")
            )
        }
        binding.eyebrowText.text = content.eyebrow
        binding.titleText.text = content.title
        binding.subtitleText.text = content.subtitle
        binding.summaryTitleText.text = content.summaryTitle
        binding.summaryDetailText.text = content.summaryDetail
        binding.summaryProgress.progress = content.progress
        binding.summaryButton.text = content.summaryAction
        binding.listTitleText.text = content.listTitle
        bindCard(binding.cardOneTitle, binding.cardOneDetail, binding.cardOneButton, content.cardOne, 1)
        bindCard(binding.cardTwoTitle, binding.cardTwoDetail, binding.cardTwoButton, content.cardTwo, 2)
        bindCard(binding.cardThreeTitle, binding.cardThreeDetail, binding.cardThreeButton, content.cardThree, 3)
        binding.summaryButton.setOnClickListener { openDetail(0) }
    }

    private fun bindCard(title: android.widget.TextView, detail: android.widget.TextView, button: Button, card: Card, index: Int) {
        title.text = card.title
        detail.text = card.detail
        button.text = card.action
        button.setOnClickListener { openDetail(index) }
    }

    private fun openDetail(index: Int) {
        if (section == Section.ACHIEVEMENTS && (index == 0 || index == 3)) {
            startActivity(Intent(this, AchievementVideosActivity::class.java))
            return
        }
        if (section == Section.ACHIEVEMENTS && index == 2) {
            startActivity(Intent(this, ProgressHistoryActivity::class.java))
            return
        }
        startActivity(DemoSession.fill(Intent(this, FeatureDetailActivity::class.java)).apply {
            putExtra(FeatureDetailActivity.EXTRA_SECTION, section.key)
            putExtra(FeatureDetailActivity.EXTRA_ITEM, index)
        })
    }

    override fun onStart() {
        super.onStart()
        requests.listener = ::renderRequest
        renderRequest(requests.state)
    }
    override fun onStop() { requests.listener = null; super.onStop() }

    private fun renderRequest(state: ProfileRequestViewModel.State) {
        val busy = state is ProfileRequestViewModel.State.Loading
        binding.summaryButton.isEnabled = !busy
        binding.exitDemoButton.isEnabled = !busy
        if (section == Section.PROFILE) binding.summaryButton.setText(if (busy) R.string.cloud_wait else R.string.demo_edit_profile)
        when (state) {
            is ProfileRequestViewModel.State.Ready -> {
                requests.consume()
                if (state.profile.uid == DemoSession.cloudUid && CloudProfiles.auth.currentUser?.uid == state.profile.uid) {
                    DemoSession.profile.clear()
                    DemoSession.profile.putAll(state.profile.fields)
                    DemoSession.fill(intent)
                    if (section == Section.PROFILE) renderProfile()
                }
            }
            is ProfileRequestViewModel.State.Failed -> {
                requests.consume()
                AlertDialog.Builder(this).setTitle(R.string.cloud_error_title).setMessage(CloudErrors.message(state.error))
                    .setPositiveButton(R.string.dialog_accept, null).show()
            }
            else -> Unit
        }
    }

    private fun configureNavigation() {
        binding.bottomNavigation.selectedItemId = section.menuId
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            if (item.itemId == section.menuId) return@setOnItemSelectedListener true
            if (item.itemId == R.id.nav_community) {
                startActivity(RegistrationKeys.copyTo(intent, Intent(this, MainActivity::class.java))
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
            } else {
                startActivity(intentFor(Section.fromMenu(item.itemId)))
            }
            finish()
            true
        }
    }

    private fun intentFor(value: Section) = RegistrationKeys.copyTo(intent,
        Intent(this, SectionActivity::class.java)).putExtra(EXTRA_SECTION, value.key)

    enum class Section(val key: String, val menuId: Int) {
        ROUTINE("routine", R.id.nav_routine), FOOD("food", R.id.nav_food), ACHIEVEMENTS("achievements", R.id.nav_achievements), PROFILE("profile", R.id.nav_profile);
        companion object {
            fun from(key: String?) = entries.firstOrNull { it.key == key } ?: ROUTINE
            fun fromMenu(id: Int) = entries.firstOrNull { it.menuId == id } ?: ROUTINE
        }
    }

    data class Card(val title: String, val detail: String, val action: String)
    data class SectionContent(val eyebrow: String, val title: String, val subtitle: String, val summaryTitle: String, val summaryDetail: String, val progress: Int, val summaryAction: String, val listTitle: String, val cardOne: Card, val cardTwo: Card, val cardThree: Card)

    companion object { const val EXTRA_SECTION = "section" }
}



