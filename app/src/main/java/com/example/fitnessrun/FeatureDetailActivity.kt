package com.example.fitnessrun

import android.content.Intent
import android.os.Bundle
import android.widget.CheckBox
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.fitnessrun.databinding.ActivityFeatureDetailBinding

class FeatureDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFeatureDetailBinding
    private lateinit var section: SectionActivity.Section
    private var routineId = 0
    private var requiredSteps = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!DemoSession.active) DemoSession.begin(intent)
        enableEdgeToEdge()
        binding = ActivityFeatureDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.detailRoot) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
        section = SectionActivity.Section.from(intent.getStringExtra(EXTRA_SECTION))
        routineId = (intent.getIntExtra(EXTRA_ITEM, 1) - 1).coerceIn(0, DemoProgress.ROUTINE_COUNT - 1)
        binding.backButton.setOnClickListener { finish() }
        binding.noticeTitleText.setText(R.string.demo_title)
        binding.secondaryButton.setText(R.string.demo_back)
        binding.secondaryButton.setOnClickListener { finish() }
        binding.primaryButton.isEnabled = false
        DemoSession.loadToday {
            if (isFinishing || isDestroyed) return@loadToday
            if (DemoSession.progressError != null) {
                androidx.appcompat.app.AlertDialog.Builder(this)
                    .setMessage(DemoSession.progressError)
                    .setPositiveButton("Volver") { _, _ -> finish() }
                    .setOnCancelListener { finish() }.show()
            } else {
                binding.primaryButton.isEnabled = true
                when (section) {
                    SectionActivity.Section.ROUTINE -> renderRoutine()
                    SectionActivity.Section.FOOD -> renderFood()
                    else -> renderAchievements()
                }
                if (DemoSession.isCloud) binding.itemsContainer.addView(android.widget.Button(this).apply {
                    text = "Guardar progreso / reintentar"
                    setOnClickListener { DemoSession.saveToday(this@FeatureDetailActivity, notifySuccess = true) }
                })
            }
        }
    }

    private fun renderRoutine(): Unit = with(binding) {
        val plan = PersonalizationEngine.weekdayRoutines()
        val selectedDays = if (routineId == 1) plan.drop(3) else plan.take(3)
        categoryText.setText(R.string.nav_routine)
        titleText.text = if (routineId == 1) "Plan de jueves y viernes" else "Plan de lunes a miércoles"
        subtitleText.text = "Asignado automáticamente con los datos de tu registro."
        noticeDetailText.text = "Este plan no evalúa tu condición médica. Si registraste molestias o lesiones, revisa el plan con un profesional antes de realizarlo."
        contentTitleText.text = "Marca cada día completado"
        metaOneText.text = "PLAN PERSONALIZADO"
        requiredSteps = selectedDays.size
        val checks = DemoSession.progress.exerciseChecks.getOrPut(routineId) { mutableSetOf() }
        selectedDays.forEachIndexed { index, day ->
            val check = CheckBox(this@FeatureDetailActivity).apply {
                text = "${day.day} · ${day.title}\n${day.detail}"
                textSize = 17f
                minHeight = (72 * resources.displayMetrics.density).toInt()
                isChecked = index in checks
                isEnabled = routineId !in DemoSession.progress.completedRoutines
                setOnCheckedChangeListener { _, checked ->
                    if (!DemoSession.canEditProgress(this@FeatureDetailActivity)) {
                        isChecked = index in checks
                        return@setOnCheckedChangeListener
                    }
                    if (checked) checks.add(index) else checks.remove(index)
                    DemoSession.saveToday(this@FeatureDetailActivity)
                    updateRoutineAction()
                }
            }
            itemsContainer.addView(check)
        }
        primaryButton.setOnClickListener {
            if (!DemoSession.canEditProgress(this@FeatureDetailActivity)) return@setOnClickListener
            DemoSession.progress.completeRoutine(routineId)
            DemoSession.saveToday(this@FeatureDetailActivity)
            itemsContainer.removeAllViews()
            renderRoutine()
        }
        updateRoutineAction()
    }

    private fun updateRoutineAction() {
        val complete = routineId in DemoSession.progress.completedRoutines
        val checked = DemoSession.progress.exerciseChecks[routineId]?.size ?: 0
        binding.metaTwoText.text = getString(R.string.demo_steps_progress, checked, requiredSteps)
        binding.primaryButton.setText(if (complete) R.string.demo_completed else R.string.demo_finish_routine)
        binding.primaryButton.isEnabled = !complete && checked == requiredSteps
    }

    private fun renderFood() = with(binding) {
        val meals = PersonalizationEngine.safeMeals()
        val catalog = FoodCatalog.filter(DemoSession.profile)
        meals.forEachIndexed { index, meal -> if (!meal.selectable) DemoSession.progress.completedMeals.remove(index) }
        categoryText.setText(R.string.nav_food)
        titleText.text = "Menú personalizado"
        subtitleText.text = "Preparado con tus preferencias y datos alimentarios registrados."
        noticeDetailText.text = PersonalizationEngine.exclusionsLabel()
        contentTitleText.text = "Comidas del día"
        metaOneText.text = "TUS ALIMENTOS"
        fun updateProgress() { metaTwoText.text = getString(R.string.demo_meal_progress, DemoSession.progress.completedMeals.size) }
        updateProgress()
        meals.forEachIndexed { index, meal ->
            itemsContainer.addView(CheckBox(this@FeatureDetailActivity).apply {
                text = "${meal.day} · ${meal.title}\n${meal.detail}"
                textSize = 17f
                minHeight = (64 * resources.displayMetrics.density).toInt()
                isChecked = index in DemoSession.progress.completedMeals
                isEnabled = meal.selectable
                setOnCheckedChangeListener { _, checked ->
                    if (!DemoSession.canEditProgress(this@FeatureDetailActivity)) {
                        isChecked = index in DemoSession.progress.completedMeals
                        return@setOnCheckedChangeListener
                    }
                    if (checked) DemoSession.progress.completedMeals.add(index) else DemoSession.progress.completedMeals.remove(index)
                    DemoSession.saveToday(this@FeatureDetailActivity)
                    updateProgress()
                }
            })
        }
        fun addCatalogText(value: String, heading: Boolean = false) {
            itemsContainer.addView(TextView(this@FeatureDetailActivity).apply {
                text = value
                textSize = if (heading) 19f else 16f
                setTextColor(androidx.core.content.ContextCompat.getColor(context, R.color.fitnessrun_text_primary))
                if (heading) setTypeface(typeface, android.graphics.Typeface.BOLD)
                val space = (12 * resources.displayMetrics.density).toInt()
                setPadding(0, space, 0, space)
            })
        }
        addCatalogText(getString(R.string.food_catalog_title), true)
        addCatalogText(getString(R.string.food_catalog_notice))
        FoodCatalog.Group.entries.forEach { group ->
            val options = FoodCatalog.preferred(catalog.available, DemoSession.profile, group)
            addCatalogText(group.label, true)
            addCatalogText(options.joinToString(" · ") { it.name }.ifBlank { getString(R.string.food_no_options) })
        }
        if (catalog.excluded.isNotEmpty()) {
            addCatalogText(getString(R.string.food_excluded_title), true)
            addCatalogText(catalog.excluded.entries.joinToString("\n") { (name, reasons) -> "$name — ${reasons.joinToString()}" })
        }
        if (catalog.unresolved.isNotEmpty()) {
            addCatalogText(getString(R.string.food_review_title), true)
            addCatalogText(catalog.unresolved.joinToString("\n"))
        }
        primaryButton.setText(R.string.demo_save)
        primaryButton.setOnClickListener { DemoSession.saveToday(this@FeatureDetailActivity, notifySuccess = true) }
    }

    private fun renderAchievements() = with(binding) {
        categoryText.setText(R.string.nav_achievements)
        titleText.setText(R.string.demo_achievements_title)
        subtitleText.setText(R.string.demo_achievements_hint)
        noticeDetailText.setText(R.string.demo_banner)
        contentTitleText.setText(R.string.nav_achievements)
        metaOneText.text = getString(R.string.demo_routine_progress, DemoSession.progress.completedRoutines.size)
        metaTwoText.text = getString(R.string.demo_meal_progress, DemoSession.progress.completedMeals.size)
        listOf(
            R.string.demo_first_step to DemoSession.progress.completedRoutines.isNotEmpty(),
            R.string.demo_consistency to (DemoSession.progress.completedRoutines.size == DemoProgress.ROUTINE_COUNT),
            R.string.demo_food_badge to (DemoSession.progress.completedMeals.size == 4)
        ).forEach { (title, earned) ->
            itemsContainer.addView(TextView(this@FeatureDetailActivity).apply {
                text = getString(title) + "\n" + getString(if (earned) R.string.demo_completed else R.string.demo_pending)
                textSize = 18f
                setPadding(0, 20, 0, 20)
            })
        }
        primaryButton.setText(R.string.demo_share)
        primaryButton.setOnClickListener {
            startActivity(DemoSession.fill(Intent(this@FeatureDetailActivity, CreatePostActivity::class.java))
                .putExtra(CreatePostActivity.EXTRA_TEXT, getString(R.string.demo_share_progress, DemoSession.progress.completedRoutines.size)))
        }
    }

    companion object {
        const val EXTRA_SECTION = "detail_section"
        const val EXTRA_ITEM = "detail_item"
    }
}


