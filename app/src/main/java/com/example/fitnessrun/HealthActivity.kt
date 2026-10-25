package com.example.fitnessrun

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.fitnessrun.databinding.ActivityHealthBinding
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class HealthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHealthBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHealthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configureSystemBars()
        configureBloodTypes()
        configureConditionalQuestions()
        binding.backButton.setOnClickListener { finish() }
        binding.nextButton.setOnClickListener { validateAndContinue() }
    }

    private fun configureSystemBars() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.healthRoot) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
    }

    private fun configureBloodTypes() {
        val bloodTypes = resources.getStringArray(R.array.blood_types)
        binding.bloodTypeDropdown.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, bloodTypes)
        )
        binding.bloodTypeDropdown.setText(bloodTypes.first(), false)
    }

    private fun configureConditionalQuestions() {
        configureConditionalField(binding.conditionRadioGroup, R.id.conditionYes, binding.conditionDetailLayout)
        configureConditionalField(binding.injuryRadioGroup, R.id.injuryYes, binding.injuryDetailLayout)
        configureConditionalField(binding.allergyRadioGroup, R.id.allergyYes, binding.allergyDetailLayout)
        configureConditionalField(binding.painRadioGroup, R.id.painYes, binding.painDetailLayout)
    }

    private fun configureConditionalField(
        group: RadioGroup,
        yesButtonId: Int,
        detailLayout: TextInputLayout
    ) {
        group.setOnCheckedChangeListener { _, checkedId ->
            val shouldShow = checkedId == yesButtonId
            detailLayout.visibility = if (shouldShow) View.VISIBLE else View.GONE
            if (!shouldShow) {
                detailLayout.error = null
                detailLayout.editText?.text?.clear()
            }
        }
    }

    private fun validateAndContinue() {
        clearErrors()
        val height = binding.heightEditText.decimalValue()
        val weight = binding.weightEditText.decimalValue()

        when {
            binding.heightEditText.text.isNullOrBlank() -> showError(binding.heightInputLayout, R.string.health_height_required)
            height == null || height !in 80.0..250.0 -> showError(binding.heightInputLayout, R.string.health_height_invalid)
            binding.weightEditText.text.isNullOrBlank() -> showError(binding.weightInputLayout, R.string.health_weight_required)
            weight == null || weight !in 25.0..350.0 -> showError(binding.weightInputLayout, R.string.health_weight_invalid)
            binding.conditionRadioGroup.checkedRadioButtonId == -1 -> showRadioError(binding.conditionRadioGroup)
            needsDetail(binding.conditionRadioGroup, R.id.conditionYes, binding.conditionDetailEditText) -> showError(binding.conditionDetailLayout, R.string.health_detail_required)
            binding.injuryRadioGroup.checkedRadioButtonId == -1 -> showRadioError(binding.injuryRadioGroup)
            needsDetail(binding.injuryRadioGroup, R.id.injuryYes, binding.injuryDetailEditText) -> showError(binding.injuryDetailLayout, R.string.health_detail_required)
            binding.allergyRadioGroup.checkedRadioButtonId == -1 -> showRadioError(binding.allergyRadioGroup)
            needsDetail(binding.allergyRadioGroup, R.id.allergyYes, binding.allergyDetailEditText) -> showError(binding.allergyDetailLayout, R.string.health_detail_required)
            binding.painRadioGroup.checkedRadioButtonId == -1 -> showRadioError(binding.painRadioGroup)
            needsDetail(binding.painRadioGroup, R.id.painYes, binding.painDetailEditText) -> showError(binding.painDetailLayout, R.string.health_detail_required)
            else -> {
                val nextStep = Intent(this, FoodPreferencesActivity::class.java).apply {
                    intent.extras?.let { putExtras(it) }
                    putExtra(RegistrationKeys.HEIGHT, binding.heightEditText.text.toString())
                    putExtra(RegistrationKeys.WEIGHT, binding.weightEditText.text.toString())
                    putExtra(RegistrationKeys.BLOOD_TYPE, binding.bloodTypeDropdown.text.toString())
                    putExtra(RegistrationKeys.CONDITION, answerSummary(binding.conditionRadioGroup, R.id.conditionYes, binding.conditionDetailEditText))
                    putExtra(RegistrationKeys.INJURY, answerSummary(binding.injuryRadioGroup, R.id.injuryYes, binding.injuryDetailEditText))
                    putExtra(RegistrationKeys.ALLERGY, answerSummary(binding.allergyRadioGroup, R.id.allergyYes, binding.allergyDetailEditText))
                    putExtra(RegistrationKeys.PAIN, answerSummary(binding.painRadioGroup, R.id.painYes, binding.painDetailEditText))
                }
                startActivity(nextStep)
            }
        }
    }

    private fun TextInputEditText.decimalValue(): Double? =
        text?.toString()?.replace(',', '.')?.toDoubleOrNull()

    private fun needsDetail(group: RadioGroup, yesId: Int, field: TextInputEditText): Boolean =
        group.checkedRadioButtonId == yesId && field.text.isNullOrBlank()

    private fun answerSummary(group: RadioGroup, yesId: Int, field: TextInputEditText): String =
        if (group.checkedRadioButtonId == yesId) {
            field.text?.toString()?.trim().orEmpty()
        } else {
            getString(R.string.summary_none)
        }

    private fun showError(layout: TextInputLayout, messageId: Int) {
        layout.error = getString(messageId)
        layout.editText?.requestFocus()
    }

    private fun showRadioError(group: RadioGroup) {
        group.requestFocus()
        group.contentDescription = getString(R.string.health_answer_required)
        Toast.makeText(this, R.string.health_answer_required, Toast.LENGTH_SHORT).show()
    }

    private fun clearErrors() {
        listOf(
            binding.heightInputLayout,
            binding.weightInputLayout,
            binding.conditionDetailLayout,
            binding.injuryDetailLayout,
            binding.allergyDetailLayout,
            binding.painDetailLayout
        ).forEach { it.error = null }
    }
}
