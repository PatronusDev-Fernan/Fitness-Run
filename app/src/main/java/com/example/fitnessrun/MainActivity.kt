package com.example.fitnessrun

import android.os.Bundle
import android.content.Intent
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import android.view.View
import android.widget.TextView
import android.widget.ImageView
import android.widget.VideoView
import android.widget.MediaController
import android.net.Uri
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.button.MaterialButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.fitnessrun.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!DemoSession.active) DemoSession.begin(intent)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        configureCommunityActions()
        DemoSession.loadToday {
            runOnUiThread {
                renderFeed()
                renderReactions()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        DemoSession.fill(intent)
        val name = DemoSession.name
        binding.greetingText.text = if (name.isBlank()) getString(R.string.community_greeting)
            else getString(R.string.community_greeting_named, name)
        renderFeed()
        renderReactions()
    }

    private fun configureCommunityActions() {
        binding.bottomNavigation.selectedItemId = R.id.nav_community
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            if (item.itemId == R.id.nav_community) true else {
                val section = SectionActivity.Section.fromMenu(item.itemId)
                startActivity(RegistrationKeys.copyTo(intent, Intent(this, SectionActivity::class.java))
                    .putExtra(SectionActivity.EXTRA_SECTION, section.key))
                false
            }
        }
        binding.searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                renderFeed()
                true
            } else false
        }
        binding.searchEditText.doAfterTextChanged { renderFeed() }
        binding.photoButton.setOnClickListener { openPost(getString(R.string.community_photo)) }
        binding.videoButton.setOnClickListener { openPost(getString(R.string.community_video)) }
        binding.progressButton.setOnClickListener { openPost(getString(R.string.community_progress)) }
        binding.notificationsButton.setOnClickListener {
            AlertDialog.Builder(this).setTitle(R.string.community_notifications)
                .setMessage(getString(R.string.demo_notifications, DemoSession.progress.completedRoutines.size,
                    DemoSession.progress.completedMeals.size, DemoSession.posts.size))
                .setPositiveButton(R.string.dialog_accept, null).show()
        }
        binding.viewCoachButton.setOnClickListener {
            startActivity(DemoSession.fill(Intent(this, CoachProfileActivity::class.java)))
        }
        binding.congratulateButton.setOnClickListener { DemoSession.congratulated = !DemoSession.congratulated; renderReactions() }
        binding.followButton.setOnClickListener { DemoSession.following = !DemoSession.following; renderReactions() }
        binding.supportButton.setOnClickListener { DemoSession.supported = !DemoSession.supported; renderReactions() }
        binding.saveButton.setOnClickListener { DemoSession.saved = !DemoSession.saved; renderReactions() }
    }

    private fun renderReactions() {
        binding.followButton.setText(if (DemoSession.following) R.string.community_following else R.string.community_follow)
        binding.supportButton.setText(if (DemoSession.supported) R.string.community_supported else R.string.community_support)
        binding.congratulateButton.setText(if (DemoSession.congratulated) R.string.demo_congratulated else R.string.community_congratulate)
        binding.saveButton.setText(if (DemoSession.saved) R.string.community_saved else R.string.community_save)
    }

    private fun renderFeed() {
        val query = binding.searchEditText.text?.toString()?.trim().orEmpty()
        val sampleMatches = (getString(R.string.community_user_name) + " " + getString(R.string.community_post_text))
            .contains(query, ignoreCase = true)
        binding.samplePostCard.visibility = if (sampleMatches) View.VISIBLE else View.GONE
        binding.localPostsContainer.removeAllViews()
        val posts = DemoSession.posts.filter { (DemoSession.displayName + " " + it.text).contains(query, ignoreCase = true) }
        posts.forEach { post ->
            val card = layoutInflater.inflate(R.layout.item_demo_post, binding.localPostsContainer, false)
            AvatarPhoto.show(card.findViewById<ImageView>(R.id.postAvatar), DemoSession.profile[ProfileIdentity.PHOTO])
            card.findViewById<TextView>(R.id.postAuthor).text = getString(R.string.demo_post_meta, DemoSession.displayName, post.audience)
            card.findViewById<TextView>(R.id.postBody).text = post.text
            card.findViewById<TextView>(R.id.postProgress).apply {
                visibility = if (post.progress.isNullOrBlank()) View.GONE else View.VISIBLE
                text = post.progress
            }
            card.findViewById<ImageView>(R.id.postImage).apply {
                visibility = if (post.media != null && post.mediaType == getString(R.string.demo_photo)) View.VISIBLE else View.GONE
                if (visibility == View.VISIBLE) setImageURI(Uri.parse(post.media))
            }
            val videoView = card.findViewById<VideoView>(R.id.postVideo)
            videoView.visibility = if (post.media != null && post.mediaType == getString(R.string.demo_video)) View.VISIBLE else View.GONE
            if (videoView.visibility == View.VISIBLE) {
                videoView.setVideoURI(Uri.parse(post.media))
                videoView.setMediaController(MediaController(this).apply { setAnchorView(videoView) })
                videoView.seekTo(1)
                videoView.setOnClickListener { if (videoView.isPlaying) videoView.pause() else videoView.start() }
            }
            card.findViewById<TextView>(R.id.postVideoHint).visibility =
                if (post.media != null && post.mediaType == getString(R.string.demo_video)) View.VISIBLE else View.GONE
            card.findViewById<MaterialButton>(R.id.postMediaButton).apply {
                visibility = View.GONE
                text = getString(R.string.demo_view_attachment, post.mediaType)
                setOnClickListener {
                    try {
                        startActivity(Intent(Intent.ACTION_VIEW).setDataAndType(Uri.parse(post.media),
                            if (post.mediaType == getString(R.string.demo_video)) "video/*" else "image/*")
                            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION))
                    } catch (_: android.content.ActivityNotFoundException) { toast(getString(R.string.demo_media_error)) }
                    catch (_: SecurityException) { toast(getString(R.string.demo_media_error)) }
                }
            }
            binding.localPostsContainer.addView(card)
        }
        binding.noResultsText.visibility = if (!sampleMatches && posts.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun openPost(type: String) = startActivity(DemoSession.fill(Intent(this, CreatePostActivity::class.java))
        .putExtra(CreatePostActivity.EXTRA_TYPE, type.lowercase()))
    private fun toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}



