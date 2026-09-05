package com.example.hive

import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge

class MainActivity : ComponentActivity() {

    companion object {
        // Point this at your local HTTP server on the tailnet
        const val DASHBOARD_URL = "http://adderstack:8080"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val webView = WebView(this).apply {
            // Keep session state across config changes
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.allowFileAccess = false

            // Let the WebView handle links internally
            webViewClient = WebViewClient()
            webChromeClient = WebChromeClient()

            loadUrl(DASHBOARD_URL)
        }

        setContentView(webView)
    }

    // Handle back button navigation within the WebView
    override fun onBackPressed() {
        val webView = findViewById<WebView>(android.R.id.content)
            ?.let { (it as? android.view.ViewGroup)?.getChildAt(0) as? WebView }

        if (webView?.canGoBack() == true) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
