package com.example.suratbebaspinjam

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import androidx.credentials.CredentialManager
import androidx.credentials.ClearCredentialStateRequest

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inisialisasi FirebaseAuth
        auth = FirebaseAuth.getInstance()

        // Inisialisasi WebView
        webView = findViewById(R.id.webView)
        setupWebView()

        // Load URL
        webView.loadUrl("https://forms.gle/m1TXrXu1rkAmcaxf6")
    }

    private fun setupWebView() {
        val webSettings: WebSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webView.webViewClient = WebViewClient() // Agar tetap di dalam WebView
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.sign_out_menu -> {
                signOut()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun signOut() {
        lifecycleScope.launch {
            auth.signOut()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) { // Android 14+
                val credentialManager = CredentialManager.create(this@MainActivity)
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            }

            startActivity(Intent(this@MainActivity, LoginActivity::class.java))
            finish()
        }
    }
}
