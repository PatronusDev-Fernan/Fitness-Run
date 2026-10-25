package com.example.fitnessrun

import android.os.Bundle
import android.widget.Toast
import android.net.Uri
import android.content.Intent
import android.widget.MediaController
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.fitnessrun.databinding.ActivityCreatePostBinding

class CreatePostActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreatePostBinding
    private var media: Uri? = null
    private var mediaType = ""
    private var progressText = ""
    private var audience = 0
    private val photoPicker = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) { persist(uri); media = uri; mediaType = getString(R.string.demo_photo); renderAttachment() }
    }
    private val videoPicker = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) { persist(uri); media = uri; mediaType = getString(R.string.demo_video); renderAttachment() }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!DemoSession.active) DemoSession.begin(intent)
        binding = ActivityCreatePostBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.postRoot) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
        val type = intent.getStringExtra(EXTRA_TYPE).orEmpty()
        if (type.isNotBlank()) binding.postEditText.hint = getString(R.string.demo_post_hint, type)
        binding.postAuthorText.text = DemoSession.displayName
        media = savedInstanceState?.getString("media")?.let(Uri::parse)
        mediaType = savedInstanceState?.getString("mediaType").orEmpty()
        progressText = savedInstanceState?.getString("progressText").orEmpty()
        audience = (savedInstanceState?.getInt("audience") ?: 0).coerceIn(0, resources.getStringArray(R.array.demo_audiences).lastIndex)
        if (savedInstanceState == null) binding.postEditText.setText(intent.getStringExtra(EXTRA_TEXT).orEmpty())
        binding.visibilityButton.text = resources.getStringArray(R.array.demo_audiences)[audience]
        renderAttachment()
        binding.cancelButton.setOnClickListener { finish() }
        binding.visibilityButton.setOnClickListener { chooseVisibility() }
        binding.addPhotoButton.setOnClickListener { photoPicker.launch(arrayOf("image/*")) }
        binding.addVideoButton.setOnClickListener { videoPicker.launch(arrayOf("video/*")) }
        binding.removeAttachmentButton.setOnClickListener { media = null; mediaType = ""; renderAttachment() }
        binding.addAchievementButton.setOnClickListener {
            chooseProgress()
        }
        binding.publishButton.setOnClickListener { publish() }
        if (savedInstanceState == null) {
            when (type.lowercase()) {
                getString(R.string.community_photo).lowercase() -> photoPicker.launch(arrayOf("image/*"))
                getString(R.string.community_video).lowercase() -> videoPicker.launch(arrayOf("video/*"))
                getString(R.string.community_progress).lowercase() -> chooseProgress()
            }
        }
    }

    private fun chooseVisibility() {
        val options = resources.getStringArray(R.array.demo_audiences)
        AlertDialog.Builder(this).setTitle(R.string.demo_audience).setSingleChoiceItems(options, audience) { dialog, index ->
            audience = index
            binding.visibilityButton.text = options[index]
            dialog.dismiss()
        }.show()
    }

    private fun publish() {
        val text = binding.postEditText.text?.toString()?.trim().orEmpty()
        binding.postInputLayout.error = if (text.isBlank()) getString(R.string.demo_post_required) else null
        if (text.isNotBlank()) {
            binding.publishButton.isEnabled = false
            DemoSession.posts.add(0, DemoSession.Post(text, resources.getStringArray(R.array.demo_audiences)[audience], media?.toString(), mediaType, progressText.ifBlank { null }))
            Toast.makeText(this, R.string.demo_post_published, Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun renderAttachment() {
        binding.attachmentText.visibility = if (media == null) View.GONE else View.VISIBLE
        binding.removeAttachmentButton.visibility = binding.attachmentText.visibility
        binding.attachmentText.text = getString(R.string.demo_attachment, mediaType)
        binding.mediaImagePreview.visibility = if (media != null && mediaType == getString(R.string.demo_photo)) View.VISIBLE else View.GONE
        binding.mediaVideoPreview.visibility = if (media != null && mediaType == getString(R.string.demo_video)) View.VISIBLE else View.GONE
        if (binding.mediaImagePreview.visibility == View.VISIBLE) binding.mediaImagePreview.setImageURI(media)
        if (binding.mediaVideoPreview.visibility == View.VISIBLE) {
            binding.mediaVideoPreview.setVideoURI(media)
            binding.mediaVideoPreview.setMediaController(MediaController(this).apply { setAnchorView(binding.mediaVideoPreview) })
            binding.mediaVideoPreview.seekTo(1)
        }
        binding.progressPreview.visibility = if (progressText.isBlank()) View.GONE else View.VISIBLE
        binding.progressPreview.text = progressText
    }

    private fun chooseProgress() {
        val completed = DemoSession.progress.completedRoutines.size
        val options = arrayOf(
            "Rutina de hoy · Completada",
            "$completed rutinas completadas esta semana",
            "20 de 30 minutos · 67%"
        )
        AlertDialog.Builder(this).setTitle("Selecciona tu avance").setItems(options) { _, index ->
            progressText = options[index]
            renderAttachment()
        }.show()
    }

    private fun persist(uri: Uri) {
        try { contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
        catch (_: SecurityException) { }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("media", media?.toString())
        outState.putString("mediaType", mediaType)
        outState.putInt("audience", audience)
        outState.putString("progressText", progressText)
        super.onSaveInstanceState(outState)
    }

    companion object {
        const val EXTRA_TYPE = "post_type"
        const val EXTRA_TEXT = "post_text"
    }
}


