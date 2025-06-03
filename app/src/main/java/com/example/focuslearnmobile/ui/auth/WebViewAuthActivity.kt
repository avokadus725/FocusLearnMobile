package com.example.focuslearnmobile.ui.auth

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.focuslearnmobile.ui.theme.FocusLearnMobileTheme
import com.example.focuslearnmobile.BuildConfig

class WebViewAuthActivity : ComponentActivity() {

    companion object {
        const val EXTRA_AUTH_PROVIDER = "auth_provider"
        const val EXTRA_AUTH_TOKEN = "auth_token"
        const val RESULT_AUTH_SUCCESS = 1001
        const val RESULT_AUTH_FAILED = 1002

        const val PROVIDER_GOOGLE = "google"
        const val PROVIDER_FACEBOOK = "facebook"
    }

    private var authProvider: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        authProvider = intent.getStringExtra(EXTRA_AUTH_PROVIDER) ?: ""

        if (authProvider.isEmpty()) {
            finishWithError("Invalid auth provider")
            return
        }

        setContent {
            FocusLearnMobileTheme {
                AuthWebViewScreen(
                    provider = authProvider,
                    onTokenReceived = { token ->
                        finishWithSuccess(token)
                    },
                    onError = { error ->
                        finishWithError(error)
                    },
                    onBack = {
                        finish()
                    }
                )
            }
        }
    }

    private fun finishWithSuccess(token: String) {
        val resultIntent = Intent().apply {
            putExtra(EXTRA_AUTH_TOKEN, token)
            putExtra(EXTRA_AUTH_PROVIDER, authProvider)
        }
        setResult(RESULT_AUTH_SUCCESS, resultIntent)
        finish()
    }

    private fun finishWithError(error: String) {
        Log.e("WebViewAuth", "Auth error: $error")
        Toast.makeText(this, error, Toast.LENGTH_LONG).show()
        setResult(RESULT_AUTH_FAILED)
        finish()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthWebViewScreen(
    provider: String,
    onTokenReceived: (String) -> Unit,
    onError: (String) -> Unit,
    onBack: () -> Unit
) {
    var isLoading by remember { mutableStateOf(true) }

    // Формуємо URL для авторизації
    val authUrl = remember(provider) {
        val baseUrl = BuildConfig.API_BASE_URL.removeSuffix("api/")
        when (provider) {
            WebViewAuthActivity.PROVIDER_GOOGLE -> "${baseUrl}api/auth/login-google"
            WebViewAuthActivity.PROVIDER_FACEBOOK -> "${baseUrl}api/auth/login-facebook"
            else -> ""
        }
    }

    Log.d("WebViewAuth", "Auth URL: $authUrl")

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = when (provider) {
                        WebViewAuthActivity.PROVIDER_GOOGLE -> "Google Auth"
                        WebViewAuthActivity.PROVIDER_FACEBOOK -> "Facebook Auth"
                        else -> "Auth"
                    }
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        )

        // Loading indicator
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    Text("Loading...")
                }
            }
        }

        // WebView
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.setSupportZoom(true)
                    settings.builtInZoomControls = false

                    webViewClient = object : WebViewClient() {

                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            super.onPageStarted(view, url, favicon)
                            Log.d("WebViewAuth", "Page started: $url")
                            isLoading = true
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            Log.d("WebViewAuth", "Page finished: $url")
                            isLoading = false

                            // Перевіряємо чи це callback URL з токеном
                            url?.let { checkForAuthCallback(it, onTokenReceived, onError) }
                        }

                        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                            Log.d("WebViewAuth", "URL loading: $url")
                            url?.let {
                                if (checkForAuthCallback(it, onTokenReceived, onError)) {
                                    return true
                                }
                            }
                            return false
                        }

                        override fun onReceivedError(
                            view: WebView?,
                            errorCode: Int,
                            description: String?,
                            failingUrl: String?
                        ) {
                            super.onReceivedError(view, errorCode, description, failingUrl)
                            Log.e("WebViewAuth", "WebView error: $description")
                            onError("Failed to load: $description")
                        }
                    }

                    // Завантажуємо URL авторизації
                    if (authUrl.isNotEmpty()) {
                        loadUrl(authUrl)
                    } else {
                        onError("Invalid auth URL")
                    }
                }
            }
        )
    }
}

/**
 * Перевіряє чи URL містить токен авторизації
 */
private fun checkForAuthCallback(
    url: String,
    onTokenReceived: (String) -> Unit,
    onError: (String) -> Unit
): Boolean {
    Log.d("WebViewAuth", "Checking callback URL: $url")

    try {
        val uri = Uri.parse(url)

        // Перевіряємо чи це callback URL
        if (url.contains("auth-callback.html") || url.contains("token=")) {
            val token = uri.getQueryParameter("token")

            if (!token.isNullOrEmpty()) {
                Log.d("WebViewAuth", "Token received: ${token.take(20)}...")
                onTokenReceived(token)
                return true
            } else {
                Log.e("WebViewAuth", "No token in callback URL")
                onError("No authentication token received")
                return true
            }
        }

        // Перевіряємо на помилки авторизації
        if (url.contains("error")) {
            val error = uri.getQueryParameter("error") ?: "Authentication failed"
            Log.e("WebViewAuth", "Auth error: $error")
            onError(error)
            return true
        }

    } catch (e: Exception) {
        Log.e("WebViewAuth", "Error parsing callback URL", e)
        onError("Failed to process authentication response")
        return true
    }

    return false
}