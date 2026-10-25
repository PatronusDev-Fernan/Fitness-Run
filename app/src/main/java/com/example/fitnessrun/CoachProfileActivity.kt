package com.example.fitnessrun

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.fitnessrun.databinding.ActivityFeatureDetailBinding
import com.google.android.material.button.MaterialButton

class CoachProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFeatureDetailBinding
    private lateinit var avatar: android.widget.ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!DemoSession.active) DemoSession.begin(intent)
        binding = ActivityFeatureDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.detailRoot) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
        with(binding) {
            categoryText.setText(R.string.coach_badge)
            titleText.setText(R.string.coach_name)
            subtitleText.setText(R.string.coach_intro)
            metaOneText.setText(R.string.coach_title)
            noticeTitleText.setText(R.string.coach_about_title)
            noticeDetailText.setText(R.string.coach_about)
            contentTitleText.setText(R.string.coach_topics_title)
            backButton.setOnClickListener { finish() }
            secondaryButton.setText(R.string.action_back)
            secondaryButton.setOnClickListener { finish() }
            primaryButton.setOnClickListener {
                if (ProfileIdentity.type(DemoSession.profile) == ProfileIdentity.COACH) {
                    startActivity(DemoSession.fill(Intent(this@CoachProfileActivity, ProfileEditorActivity::class.java)))
                } else {
                    DemoSession.followingCoach = !DemoSession.followingCoach
                    renderFollow()
                }
            }
        }
        avatar = android.widget.ImageView(this).apply {
            val size = (112 * resources.displayMetrics.density).toInt()
            layoutParams = android.widget.LinearLayout.LayoutParams(size, size)
            scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
            contentDescription = getString(R.string.profile_photo_description)
        }
        binding.itemsContainer.addView(avatar)
        addText(R.string.coach_topics)
        addText(R.string.coach_resources_title)
        addAction(R.string.coach_videos) { startActivity(Intent(this, AchievementVideosActivity::class.java)) }
        addAction(R.string.coach_routines) { openSection(SectionActivity.Section.ROUTINE) }
        addAction(R.string.coach_food) { openSection(SectionActivity.Section.FOOD) }
        addText(R.string.coach_contact_notice)
        renderFollow()
    }
    private fun addText(resource: Int) {
        binding.itemsContainer.addView(TextView(this).apply {
            setText(resource)
            textSize = 16f
            setTextColor(ContextCompat.getColor(context, R.color.fitnessrun_text_primary))
            val padding = (12 * resources.displayMetrics.density).toInt()
            setPadding(0, padding, 0, padding)
        })
    }
    private fun addAction(resource: Int, action: () -> Unit) {
        binding.itemsContainer.addView(MaterialButton(this).apply {
            setText(resource)
            setOnClickListener { action() }
        })
    }
    private fun renderFollow() {
        if (ProfileIdentity.type(DemoSession.profile) == ProfileIdentity.COACH) {
            binding.primaryButton.setText(R.string.profile_edit_title)
            binding.metaTwoText.setText(R.string.profile_coach_badge)
            return
        }
        binding.primaryButton.setText(if (DemoSession.followingCoach) R.string.coach_unfollow else R.string.coach_follow)
        binding.metaTwoText.setText(if (DemoSession.followingCoach) R.string.coach_following else R.string.coach_not_following)
    }
    override fun onResume() {
        super.onResume()
        val coach = ProfileIdentity.type(DemoSession.profile) == ProfileIdentity.COACH
        avatar.visibility = if (coach) android.view.View.VISIBLE else android.view.View.GONE
        if (coach) {
            binding.categoryText.setText(R.string.profile_coach_badge)
            binding.titleText.text = DemoSession.fullName
            binding.subtitleText.text = DemoSession.profile[ProfileIdentity.BIO]?.takeIf { it.isNotBlank() } ?: getString(R.string.profile_bio_empty)
            binding.noticeTitleText.setText(R.string.profile_my_coach)
            binding.noticeDetailText.setText(R.string.profile_type_notice)
            AvatarPhoto.show(avatar, DemoSession.profile[ProfileIdentity.PHOTO])
        }
        renderFollow()
    }
    private fun openSection(section: SectionActivity.Section) {
        startActivity(DemoSession.fill(Intent(this, SectionActivity::class.java)).putExtra(SectionActivity.EXTRA_SECTION, section.key))
    }
}
