package com.example.suratbebaspinjam

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import androidx.credentials.CredentialManager
import androidx.credentials.ClearCredentialStateRequest

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private var fileUploadCallback: ValueCallback<Array<Uri>>? = null
    private val FILE_PICKER_REQUEST_CODE = 1000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inisialisasi FirebaseAuth
        auth = FirebaseAuth.getInstance()

        // Cek apakah pengguna sudah login
        val user = auth.currentUser
        if (user == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // Tombol untuk membuka Google Forms dengan Chrome Custom Tabs
        val btnOpenForm = findViewById<Button>(R.id.btnOpenForm)
        btnOpenForm.setOnClickListener {
            openGoogleForm()
        }

        // Setup WebView
        val webView = findViewById<WebView>(R.id.webView)
        setupWebView(webView)
    }

    private fun openGoogleForm() {
        val url = "https://docs.google.com/forms/d/e/1FAIpQLSdnSmy0ITtvVBySmcTSXPRy3AwU9Zo8iwlRKMrPi4EsUz8mDA/viewform"

        val customTabsIntent = CustomTabsIntent.Builder()
            .setShowTitle(true)
            .build()

        customTabsIntent.launchUrl(this, Uri.parse(url))
    }

    private fun setupWebView(webView: WebView) {
        webView.settings.javaScriptEnabled = true
        webView.settings.allowFileAccess = true
        webView.settings.allowContentAccess = true
        webView.settings.domStorageEnabled = true
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                return false
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                fileUploadCallback?.onReceiveValue(null)
                fileUploadCallback = filePathCallback

                // Intent untuk membuka galeri
                val galleryIntent = Intent(Intent.ACTION_PICK)
                galleryIntent.type = "image/*"

                // Intent untuk semua jenis file
                val fileIntent = Intent(Intent.ACTION_GET_CONTENT)
                fileIntent.addCategory(Intent.CATEGORY_OPENABLE)
                fileIntent.type = "*/*"

                // Gabungkan intent dalam chooser
                val chooserIntent = Intent.createChooser(fileIntent, "Pilih File")
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(galleryIntent))

                startActivityForResult(chooserIntent, FILE_PICKER_REQUEST_CODE)
                return true
            }
        }

        webView.loadUrl("https://docs.google.com/forms/d/e/1FAIpQLSdnSmy0ITtvVBySmcTSXPRy3AwU9Zo8iwlRKMrPi4EsUz8mDA/viewform")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == FILE_PICKER_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                val result = if (data.data != null) arrayOf(data.data!!) else null
                fileUploadCallback?.onReceiveValue(result)
            } else {
                fileUploadCallback?.onReceiveValue(null)
            }
            fileUploadCallback = null
        }
    }

    override fun onCreateOptionsMenu(menu: android.view.Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
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

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                val credentialManager = CredentialManager.create(this@MainActivity)
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            }

            startActivity(Intent(this@MainActivity, LoginActivity::class.java))
            finish()
        }
    }
}
