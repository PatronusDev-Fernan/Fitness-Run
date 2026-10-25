package com.example.fitnessrun

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.fitnessrun.databinding.ActivityAchievementVideosBinding

class AchievementVideosActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAchievementVideosBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAchievementVideosBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.videosRoot) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
        binding.backButton.setOnClickListener { finish() }
        binding.playWarmupButton.setOnClickListener { play("Calentamiento antes de entrenar", "HNORmHn16gY") }
        binding.playMobilityButton.setOnClickListener { play("Movilidad para recuperación", "YQQfhILVR7c") }
    }

    private fun play(title: String, videoId: String) {
        startActivity(Intent(this, VideoPlayerActivity::class.java).apply {
            putExtra(VideoPlayerActivity.EXTRA_TITLE, title)
            putExtra(VideoPlayerActivity.EXTRA_VIDEO_ID, videoId)
        })
    }
}
