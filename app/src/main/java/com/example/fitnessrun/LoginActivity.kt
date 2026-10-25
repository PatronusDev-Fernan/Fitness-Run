package com.example.fitnessrun

import android.os.Bundle
import android.content.Intent
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import android.util.Patterns
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.fitnessrun.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val requests by lazy { ViewModelProvider(this)[ProfileRequestViewModel::class.java] }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configureSystemBars()
        configureActions()
        binding.passwordEditText.isSaveEnabled = false
        binding.loginButton.setText(R.string.cloud_login_action)
        binding.forgotPasswordButton.setText(R.string.cloud_reset_action)
        binding.loginSubtitle.setText(R.string.cloud_profile_notice)
        // Restore an existing account only after its profile is confirmed by the server.
        if (savedInstanceState == null && requests.state is ProfileRequestViewModel.State.Idle &&
            com.google.firebase.FirebaseApp.getApps(this).isNotEmpty() && CloudProfiles.auth.currentUser != null) {
            requests.execute { CloudProfiles.load() }
        }
    }

    private fun configureSystemBars() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun configureActions() {
        binding.loginButton.setOnClickListener { validateAndContinue() }
        binding.passwordEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                validateAndContinue()
                true
            } else {
                false
            }
        }

        binding.forgotPasswordButton.setOnClickListener {
            val email = binding.userEditText.text?.toString()?.trim().orEmpty()
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.userInputLayout.error = getString(R.string.register_email_invalid)
            } else requests.execute {
                CloudProfiles.auth.sendPasswordResetEmail(email).continueWith { result ->
                    result.getResult(Exception::class.java)
                    CloudProfile("", emptyMap())
                }
            }
        }
        binding.googleLoginButton.setOnClickListener { enterDemo() }
        binding.createAccountButton.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java).putExtra(ProfileIdentity.TYPE, selectedAccountType()))
        }
    }

    private fun validateAndContinue() {
        val user = binding.userEditText.text?.toString()?.trim().orEmpty()
        val password = binding.passwordEditText.text?.toString().orEmpty()

        binding.userInputLayout.error = null
        binding.passwordInputLayout.error = null

        when {
            user.isBlank() -> {
                binding.userInputLayout.error = getString(R.string.login_user_required)
                binding.userEditText.requestFocus()
            }
            !Patterns.EMAIL_ADDRESS.matcher(user).matches() -> {
                binding.userInputLayout.error = getString(R.string.register_email_invalid)
                binding.userEditText.requestFocus()
            }
            password.isBlank() -> {
                binding.passwordInputLayout.error = getString(R.string.login_password_required)
                binding.passwordEditText.requestFocus()
            }
            password.length < MINIMUM_PASSWORD_LENGTH -> {
                binding.passwordInputLayout.error = getString(R.string.login_password_too_short)
                binding.passwordEditText.requestFocus()
            }
            else -> {
                val accountType = selectedAccountType()
                requests.execute { CloudProfiles.signIn(user, password, accountType) }
            }
        }
    }

    private fun enterDemo() {
        if (com.google.firebase.FirebaseApp.getApps(this).isNotEmpty()) CloudProfiles.auth.signOut()
        RegistrationDraft.clear()
        DemoSession.beginExample()
        DemoSession.profile[ProfileIdentity.TYPE] = selectedAccountType()
        binding.passwordEditText.text?.clear()
        startActivity(DemoSession.fill(Intent(this, MainActivity::class.java)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
    }

    override fun onStart() {
        super.onStart()
        requests.listener = ::renderRequest
        renderRequest(requests.state)
    }

    override fun onStop() {
        requests.listener = null
        super.onStop()
    }

    private fun renderRequest(state: ProfileRequestViewModel.State) {
        val busy = state is ProfileRequestViewModel.State.Loading
        listOf(binding.loginButton, binding.googleLoginButton, binding.createAccountButton,
            binding.forgotPasswordButton, binding.userEditText, binding.passwordEditText,
            binding.accountUser, binding.accountCoach).forEach { it.isEnabled = !busy }
        binding.loginButton.setText(if (busy) R.string.cloud_wait else R.string.cloud_login_action)
        when (state) {
            is ProfileRequestViewModel.State.Ready -> {
                requests.consume()
                if (state.profile.uid.isEmpty()) {
                    AlertDialog.Builder(this).setMessage(R.string.cloud_reset_sent).setPositiveButton(R.string.dialog_accept, null).show()
                } else {
                    DemoSession.beginCloud(state.profile)
                    binding.passwordEditText.text?.clear()
                    startActivity(DemoSession.fill(Intent(this, MainActivity::class.java)).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
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

    private companion object {
        const val MINIMUM_PASSWORD_LENGTH = 8
    }

    private fun selectedAccountType() = if (binding.accountCoach.isChecked) ProfileIdentity.COACH else ProfileIdentity.USER
}
