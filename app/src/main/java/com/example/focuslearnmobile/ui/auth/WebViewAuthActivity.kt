package com.example.focuslearnmobile.ui.auth

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.webkit.*
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Формуємо URL для авторизації
    val authUrl = remember(provider) {
        when (provider) {
            WebViewAuthActivity.PROVIDER_GOOGLE -> "${BuildConfig.API_BASE_URL}Auth/login-google"
            WebViewAuthActivity.PROVIDER_FACEBOOK -> "${BuildConfig.API_BASE_URL}Auth/login-facebook"
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
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
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

        // Error message
        errorMessage?.let { error ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = error,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }

        // WebView
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                WebView(context).apply {
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        setSupportZoom(true)
                        builtInZoomControls = false
                        // Додаткові налаштування для кращої сумісності
                        mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                        useWideViewPort = true
                        loadWithOverviewMode = true
                        allowFileAccess = true
                        allowContentAccess = true
                        userAgentString = "Mozilla/5.0 (Linux; Android 11; Pixel 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Mobile Safari/537.36"
                    }

                    webViewClient = object : WebViewClient() {

                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            super.onPageStarted(view, url, favicon)
                            Log.d("WebViewAuth", "Page started: $url")
                            isLoading = true
                            errorMessage = null
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            Log.d("WebViewAuth", "Page finished: $url")
                            isLoading = false

                            // Перевіряємо чи це callback URL з токеном
                            url?.let { checkForAuthCallback(it, onTokenReceived, onError) }
                        }

                        // ТІЛЬКИ ОДИН shouldOverrideUrlLoading метод!
                        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                            val url = request?.url?.toString()
                            Log.d("WebViewAuth", "URL loading: $url")

                            // Додайте заголовки для Google запитів
                            if (url?.contains("accounts.google.com") == true) {
                                val headers = mapOf(
                                    "User-Agent" to "Mozilla/5.0 (Linux; Android 11; Pixel 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Mobile Safari/537.36"
                                )
                                view?.loadUrl(url, headers)
                                return true
                            }

                            url?.let {
                                if (checkForAuthCallback(it, onTokenReceived, onError)) {
                                    return true
                                }
                            }
                            return false
                        }

                        override fun onReceivedError(
                            view: WebView?,
                            request: WebResourceRequest?,
                            error: WebResourceError?
                        ) {
                            super.onReceivedError(view, request, error)
                            val errorDesc = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                                error?.description?.toString() ?: "Unknown error"
                            } else {
                                "Connection error"
                            }

                            Log.e("WebViewAuth", "WebView error: $errorDesc")

                            // Показуємо помилку тільки для основної URL
                            if (request?.url?.toString()?.contains(BuildConfig.API_BASE_URL) == true) {
                                errorMessage = "Connection failed: $errorDesc\nPlease check if the server is running on ${BuildConfig.API_BASE_URL}"
                                isLoading = false
                            }
                        }

                        @Deprecated("Deprecated in Java")
                        override fun onReceivedError(
                            view: WebView?,
                            errorCode: Int,
                            description: String?,
                            failingUrl: String?
                        ) {
                            super.onReceivedError(view, errorCode, description, failingUrl)
                            Log.e("WebViewAuth", "WebView error (legacy): $description")

                            if (failingUrl?.contains(BuildConfig.API_BASE_URL) == true) {
                                errorMessage = "Connection failed: $description\nPlease check if the server is running"
                                isLoading = false
                            }
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
        if (url.contains("auth-callback") || url.contains("/callback") || url.contains("token=")) {
            // Спробуємо знайти токен в URL параметрах
            val token = uri.getQueryParameter("token")
                ?: uri.getQueryParameter("access_token")
                ?: uri.getQueryParameter("authToken")

            if (!token.isNullOrEmpty()) {
                Log.d("WebViewAuth", "Token received: ${token.take(20)}...")
                onTokenReceived(token)
                return true
            } else {
                // Можливо токен в fragment
                val fragment = uri.fragment
                if (!fragment.isNullOrEmpty()) {
                    val fragmentParams = fragment.split("&")
                    for (param in fragmentParams) {
                        val parts = param.split("=")
                        if (parts.size == 2 && (parts[0] == "token" || parts[0] == "access_token")) {
                            Log.d("WebViewAuth", "Token from fragment: ${parts[1].take(20)}...")
                            onTokenReceived(parts[1])
                            return true
                        }
                    }
                }

                Log.e("WebViewAuth", "No token in callback URL")
                onError("No authentication token received")
                return true
            }
        }

        // Перевіряємо на помилки авторизації
        if (url.contains("error") || url.contains("auth-failed")) {
            val error = uri.getQueryParameter("error")
                ?: uri.getQueryParameter("error_description")
                ?: "Authentication failed"
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