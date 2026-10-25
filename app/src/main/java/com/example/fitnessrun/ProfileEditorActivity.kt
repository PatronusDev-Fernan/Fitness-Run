package com.example.fitnessrun

import android.os.Bundle
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.fitnessrun.databinding.ActivityProfileEditorBinding
import com.google.android.gms.tasks.Tasks

class ProfileEditorActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileEditorBinding
    private val requests by lazy { ViewModelProvider(this)[ProfileRequestViewModel::class.java] }
    private var photo = ""
    private val picker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            val appContext = applicationContext
            requests.execute { Tasks.call(AvatarPhoto.executor) { CloudProfile("", mapOf(ProfileIdentity.PHOTO to AvatarPhoto.encode(appContext, uri))) } }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!DemoSession.active) DemoSession.begin(intent)
        binding = ActivityProfileEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.editorRoot) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
        photo = savedInstanceState?.getString("photo") ?: DemoSession.profile[ProfileIdentity.PHOTO].orEmpty()
        with(binding) {
            accountBadge.text = ProfileIdentity.badge(DemoSession.profile)
            editName.setText(DemoSession.profile[RegistrationKeys.NAME])
            editLastName.setText(DemoSession.profile[RegistrationKeys.LAST_NAME])
            editGoal.setText(DemoSession.profile[RegistrationKeys.FOOD_GOAL])
            editBio.setText(DemoSession.profile[ProfileIdentity.BIO])
            choosePhotoButton.setOnClickListener { picker.launch("image/*") }
            removePhotoButton.setOnClickListener { photo = ""; AvatarPhoto.show(profilePhoto, photo) }
            backButton.setOnClickListener { finish() }
            saveButton.setOnClickListener { save() }
            AvatarPhoto.show(profilePhoto, photo)
        }
        onBackPressedDispatcher.addCallback(this) { if (requests.state !is ProfileRequestViewModel.State.Loading) finish() }
    }

    private fun save() {
        val fields = listOf(binding.editName, binding.editLastName, binding.editGoal)
        val empty = fields.firstOrNull { it.text.isNullOrBlank() }
        if (empty != null) { empty.error = getString(R.string.demo_fields_required); return }
        val changes = listOf(RegistrationKeys.NAME, RegistrationKeys.LAST_NAME, RegistrationKeys.FOOD_GOAL)
            .zip(fields).associate { (key, field) -> key to field.text.toString().trim() } +
            mapOf(ProfileIdentity.BIO to binding.editBio.text.toString().trim(), ProfileIdentity.PHOTO to photo)
        val uid = DemoSession.cloudUid
        if (uid == null) {
            DemoSession.profile.putAll(ProfileFields.editIdentity(DemoSession.profile, changes))
            finish()
        } else requests.execute { CloudProfiles.updateIdentity(uid, changes) }
    }

    override fun onSaveInstanceState(outState: Bundle) { outState.putString("photo", photo); super.onSaveInstanceState(outState) }
    override fun onStart() { super.onStart(); requests.listener = ::render; render(requests.state) }
    override fun onStop() { requests.listener = null; super.onStop() }

    private fun render(state: ProfileRequestViewModel.State) {
        val busy = state is ProfileRequestViewModel.State.Loading
        listOf(binding.saveButton, binding.backButton, binding.choosePhotoButton, binding.removePhotoButton,
            binding.editName, binding.editLastName, binding.editGoal, binding.editBio).forEach { it.isEnabled = !busy }
        binding.saveButton.setText(if (busy) R.string.cloud_wait else R.string.demo_save)
        when (state) {
            is ProfileRequestViewModel.State.Ready -> {
                requests.consume()
                if (state.profile.uid.isEmpty()) {
                    photo = state.profile.fields[ProfileIdentity.PHOTO].orEmpty()
                    AvatarPhoto.show(binding.profilePhoto, photo)
                } else if (state.profile.uid == DemoSession.cloudUid && CloudProfiles.auth.currentUser?.uid == state.profile.uid) {
                    DemoSession.profile.clear()
                    DemoSession.profile.putAll(state.profile.fields)
                    Toast.makeText(this, R.string.profile_saved, Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            is ProfileRequestViewModel.State.Failed -> {
                requests.consume()
                val message = if (state.error is IllegalArgumentException || state.error is java.io.IOException || state.error is SecurityException)
                    R.string.profile_photo_error else CloudErrors.message(state.error)
                AlertDialog.Builder(this).setMessage(message).setPositiveButton(R.string.dialog_accept, null).show()
            }
            else -> Unit
        }
    }
}
