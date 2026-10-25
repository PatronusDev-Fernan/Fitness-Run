package com.example.fitnessrun

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.fitnessrun.databinding.ActivityRegisterBinding
import java.text.DateFormat
import java.util.Calendar

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        if (savedInstanceState == null) RegistrationDraft.clear()
        binding.passwordEditText.isSaveEnabled = false
        binding.confirmPasswordEditText.isSaveEnabled = false
        binding.termsCheckbox.setText(R.string.cloud_register_terms)
        binding.registerSubtitle.setText(if (intent.getStringExtra(ProfileIdentity.TYPE) == ProfileIdentity.COACH)
            R.string.register_coach_subtitle else R.string.register_subtitle)
        binding.nameEditText.filters = arrayOf(android.text.InputFilter.LengthFilter(80))
        binding.lastNameEditText.filters = arrayOf(android.text.InputFilter.LengthFilter(80))
        setContentView(binding.root)

        configureSystemBars()
        configureActions()
    }

    private fun configureSystemBars() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.registerRoot) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun configureActions() {
        binding.createAccountButton.setOnClickListener { validateRegistration() }
        binding.birthDateEditText.setOnClickListener { showBirthDatePicker() }
        binding.confirmPasswordEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                validateRegistration()
                true
            } else {
                false
            }
        }
        binding.backToLoginButton.setOnClickListener { finish() }
    }

    private fun showBirthDatePicker() {
        val today = Calendar.getInstance()
        val initialDate = Calendar.getInstance().apply {
            add(Calendar.YEAR, -18)
        }

        DatePickerDialog(
            this,
            { _, year, month, day ->
                val selectedDate = Calendar.getInstance().apply {
                    set(year, month, day)
                }
                binding.birthDateEditText.setText(
                    DateFormat.getDateInstance(DateFormat.MEDIUM).format(selectedDate.time)
                )
                binding.birthDateInputLayout.error = null
            },
            initialDate.get(Calendar.YEAR),
            initialDate.get(Calendar.MONTH),
            initialDate.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.maxDate = today.timeInMillis
            show()
        }
    }

    private fun validateRegistration() {
        val name = binding.nameEditText.text?.toString()?.trim().orEmpty()
        val lastName = binding.lastNameEditText.text?.toString()?.trim().orEmpty()
        val birthDate = binding.birthDateEditText.text?.toString()?.trim().orEmpty()
        val email = binding.emailEditText.text?.toString()?.trim().orEmpty()
        val password = binding.passwordEditText.text?.toString().orEmpty()
        val confirmation = binding.confirmPasswordEditText.text?.toString().orEmpty()

        clearErrors()

        when {
            name.isBlank() -> showNameError(R.string.register_name_required)
            lastName.isBlank() -> showLastNameError()
            birthDate.isBlank() -> showBirthDateError()
            email.isBlank() -> showEmailError(R.string.register_email_required)
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                showEmailError(R.string.register_email_invalid)
            password.isBlank() -> showPasswordError(R.string.register_password_required)
            password.length < MINIMUM_PASSWORD_LENGTH ->
                showPasswordError(R.string.register_password_too_short)
            confirmation.isBlank() -> showConfirmationError(R.string.register_confirm_required)
            password != confirmation -> showConfirmationError(R.string.register_password_mismatch)
            !binding.termsCheckbox.isChecked ->
                binding.termsCheckbox.error = getString(R.string.cloud_register_terms_required)
            else -> {
                RegistrationDraft.set(email, password)
                val nextStep = Intent(this, HealthActivity::class.java).apply {
                    putExtra(RegistrationKeys.NAME, name)
                    putExtra(RegistrationKeys.LAST_NAME, lastName)
                    putExtra(RegistrationKeys.BIRTH_DATE, birthDate)
                    putExtra(RegistrationKeys.EMAIL, email)
                    putExtra(ProfileIdentity.TYPE, if (intent.getStringExtra(ProfileIdentity.TYPE) == ProfileIdentity.COACH)
                        ProfileIdentity.COACH else ProfileIdentity.USER)
                }
                startActivity(nextStep)
            }
        }
    }

    private fun clearErrors() {
        binding.nameInputLayout.error = null
        binding.lastNameInputLayout.error = null
        binding.birthDateInputLayout.error = null
        binding.emailInputLayout.error = null
        binding.passwordInputLayout.error = null
        binding.confirmPasswordInputLayout.error = null
        binding.termsCheckbox.error = null
    }

    private fun showLastNameError() {
        binding.lastNameInputLayout.error = getString(R.string.register_last_name_required)
        binding.lastNameEditText.requestFocus()
    }

    private fun showBirthDateError() {
        binding.birthDateInputLayout.error = getString(R.string.register_birth_date_required)
        binding.birthDateEditText.requestFocus()
    }

    private fun showNameError(message: Int) {
        binding.nameInputLayout.error = getString(message)
        binding.nameEditText.requestFocus()
    }

    private fun showEmailError(message: Int) {
        binding.emailInputLayout.error = getString(message)
        binding.emailEditText.requestFocus()
    }

    private fun showPasswordError(message: Int) {
        binding.passwordInputLayout.error = getString(message)
        binding.passwordEditText.requestFocus()
    }

    private fun showConfirmationError(message: Int) {
        binding.confirmPasswordInputLayout.error = getString(message)
        binding.confirmPasswordEditText.requestFocus()
    }

    private companion object {
        const val MINIMUM_PASSWORD_LENGTH = 8
    }

    override fun onDestroy() {
        if (isFinishing) RegistrationDraft.clear()
        super.onDestroy()
    }
}
