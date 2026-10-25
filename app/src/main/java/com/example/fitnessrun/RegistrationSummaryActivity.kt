package com.example.fitnessrun

import android.os.Bundle
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.textfield.TextInputEditText
import android.text.InputType
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.fitnessrun.databinding.ActivityRegistrationSummaryBinding

class RegistrationSummaryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistrationSummaryBinding
    private val requests by lazy { ViewModelProvider(this)[ProfileRequestViewModel::class.java] }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrationSummaryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.summaryRoot) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        showSummary()
        binding.backButton.setOnClickListener { finish() }
        binding.confirmButton.setText(R.string.cloud_register_action)
        binding.confirmButton.setOnClickListener { confirmRegistration() }
    }

    private fun confirmRegistration() {
        val password = RegistrationDraft.take(value(RegistrationKeys.EMAIL))
        if (password != null) { submit(password); return }
        val input = TextInputEditText(this).apply {
            hint = getString(R.string.login_password_hint)
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            isSaveEnabled = false
        }
        val dialog = AlertDialog.Builder(this).setTitle(R.string.cloud_password_again).setView(input)
            .setNegativeButton(R.string.dialog_cancel, null).setPositiveButton(R.string.cloud_register_action, null).create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val entered = input.text?.toString().orEmpty()
                if (entered.length < 8) input.error = getString(R.string.register_password_too_short)
                else { input.text?.clear(); dialog.dismiss(); submit(entered) }
            }
        }
        dialog.show()
    }

    private fun submit(password: String) {
        val extras = RegistrationKeys.copyTo(intent, Intent()).extras
        val fields = ProfileFields.allowed.mapNotNull { key -> extras?.getString(key)?.let { key to it } }.toMap()
        requests.execute { CloudProfiles.register(fields, password) }
    }

    override fun onStart() {
        super.onStart()
        requests.listener = ::renderRequest
        renderRequest(requests.state)
    }
    override fun onStop() { requests.listener = null; super.onStop() }

    private fun renderRequest(state: ProfileRequestViewModel.State) {
        val busy = state is ProfileRequestViewModel.State.Loading
        binding.confirmButton.isEnabled = !busy
        binding.backButton.isEnabled = !busy
        binding.confirmButton.setText(if (busy) R.string.cloud_wait else R.string.cloud_register_action)
        when (state) {
            is ProfileRequestViewModel.State.Ready -> {
                requests.consume()
                DemoSession.beginCloud(state.profile)
                startActivity(DemoSession.fill(Intent(this, MainActivity::class.java)).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
            }
            is ProfileRequestViewModel.State.Failed -> {
                requests.consume()
                AlertDialog.Builder(this).setTitle(R.string.cloud_error_title).setMessage(CloudErrors.message(state.error))
                    .setPositiveButton(R.string.dialog_accept, null).show()
            }
            else -> Unit
        }
    }

    private fun showSummary() {
        val fullName = listOf(value(RegistrationKeys.NAME), value(RegistrationKeys.LAST_NAME))
            .filter { it != getString(R.string.summary_not_provided) }
            .joinToString(" ")
            .ifBlank { getString(R.string.summary_not_provided) }

        binding.nameText.text = getString(R.string.summary_name, fullName) + " · " + ProfileIdentity.badge(mapOf(ProfileIdentity.TYPE to intent.getStringExtra(ProfileIdentity.TYPE).orEmpty()))
        binding.birthDateText.text = getString(R.string.summary_birth_date, value(RegistrationKeys.BIRTH_DATE))
        binding.emailText.text = getString(R.string.summary_email, value(RegistrationKeys.EMAIL))
        binding.measurementsText.text = getString(
            R.string.summary_measurements,
            value(RegistrationKeys.HEIGHT),
            value(RegistrationKeys.WEIGHT)
        )
        binding.bloodText.text = getString(R.string.summary_blood, value(RegistrationKeys.BLOOD_TYPE))
        binding.conditionText.text = getString(R.string.summary_condition, value(RegistrationKeys.CONDITION))
        binding.injuryText.text = getString(R.string.summary_injury, value(RegistrationKeys.INJURY))
        binding.allergyText.text = getString(R.string.summary_allergy, value(RegistrationKeys.ALLERGY))
        binding.painText.text = getString(R.string.summary_pain, value(RegistrationKeys.PAIN))
        binding.vegetablesText.text = getString(R.string.summary_vegetables, value(RegistrationKeys.VEGETABLES))
        binding.fruitsText.text = getString(R.string.summary_fruits, value(RegistrationKeys.FRUITS))
        binding.favoritesText.text = getString(R.string.summary_favorites, value(RegistrationKeys.FAVORITES))
        binding.dislikesText.text = getString(R.string.summary_dislikes, value(RegistrationKeys.DISLIKES))
        binding.restrictionsText.text = getString(R.string.summary_restrictions, value(RegistrationKeys.RESTRICTIONS))
        binding.dietText.text = getString(R.string.summary_diet, value(RegistrationKeys.DIET_TYPE))
        binding.goalText.text = getString(R.string.summary_goal, value(RegistrationKeys.FOOD_GOAL))
    }

    private fun value(key: String): String =
        intent.getStringExtra(key)?.takeIf { it.isNotBlank() }
            ?: getString(R.string.summary_not_provided)
}

