package com.example.fitnessrun

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.fitnessrun.databinding.ActivityVideoPlayerBinding

class VideoPlayerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityVideoPlayerBinding

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.playerRoot) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
        binding.titleText.text = intent.getStringExtra(EXTRA_TITLE).orEmpty()
        binding.backButton.setOnClickListener { finish() }
        binding.videoWebView.settings.javaScriptEnabled = true
        binding.videoWebView.settings.domStorageEnabled = true
        binding.videoWebView.settings.allowFileAccess = false
        binding.videoWebView.settings.allowContentAccess = false
        binding.videoWebView.settings.mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
        binding.videoWebView.webChromeClient = WebChromeClient()
        binding.videoWebView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean =
                !VideoUrlPolicy.isAllowed(request?.url?.toString().orEmpty())

            override fun onPageFinished(view: WebView?, url: String?) {
                binding.loadingIndicator.visibility = View.GONE
            }
        }
        val videoId = intent.getStringExtra(EXTRA_VIDEO_ID).orEmpty()
        if (!VideoUrlPolicy.isValidId(videoId)) {
            finish()
            return
        }
        binding.videoWebView.loadUrl("https://www.youtube-nocookie.com/embed/$videoId?playsinline=1&rel=0")
    }

    override fun onDestroy() {
        binding.videoWebView.apply { stopLoading(); loadUrl("about:blank"); destroy() }
        super.onDestroy()
    }

    companion object {
        const val EXTRA_TITLE = "video_title"
        const val EXTRA_VIDEO_ID = "video_id"
    }
}
