package com.example.focuslearnmobile.ui.auth

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.focuslearnmobile.R

@Composable
fun AuthScreen(
    viewModel: AuthViewModel = viewModel(),
    onAuthSuccess: () -> Unit
) {
    val context = LocalContext.current
    val authState by viewModel.authState.collectAsState()

    // Launcher для WebView авторизації
    val authLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        when (result.resultCode) {
            WebViewAuthActivity.RESULT_AUTH_SUCCESS -> {
                val token = result.data?.getStringExtra(WebViewAuthActivity.EXTRA_AUTH_TOKEN)
                //val provider = result.data?.getStringExtra(WebViewAuthActivity.EXTRA_AUTH_PROVIDER)

                if (!token.isNullOrEmpty()) {
                    viewModel.handleAuthSuccess(token)
                }
            }
            WebViewAuthActivity.RESULT_AUTH_FAILED -> {
                viewModel.handleAuthError("Authentication failed")
            }
        }
    }

    LaunchedEffect(authState.isAuthenticated) {
        if (authState.isAuthenticated) {
            onAuthSuccess()
        }
    }

    authState.error?.let { error ->
        LaunchedEffect(error) {
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        // Назва додатку
        Text(
            text = "FocusLearn",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Welcome! Please sign in to continue",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Google Sign In Button
        AuthButton(
            text = "Continue with Google",
            onClick = {
                val intent = Intent(context, WebViewAuthActivity::class.java).apply {
                    putExtra(WebViewAuthActivity.EXTRA_AUTH_PROVIDER, WebViewAuthActivity.PROVIDER_GOOGLE)
                }
                authLauncher.launch(intent)
            },
            backgroundColor = Color.White,
            textColor = Color.Black,
            iconRes = R.drawable.ic_google,
            enabled = !authState.isLoading
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Facebook Sign In Button
        AuthButton(
            text = "Continue with Facebook",
            onClick = {
                val intent = Intent(context, WebViewAuthActivity::class.java).apply {
                    putExtra(WebViewAuthActivity.EXTRA_AUTH_PROVIDER, WebViewAuthActivity.PROVIDER_FACEBOOK)
                }
                authLauncher.launch(intent)
            },
            backgroundColor = Color(0xFF1877F2),
            textColor = Color.White,
            iconRes = R.drawable.ic_facebook,
            enabled = !authState.isLoading
        )

        if (authState.isLoading) {
            Spacer(modifier = Modifier.height(24.dp))
            CircularProgressIndicator()
        }

        authState.error?.let { error ->
            Spacer(modifier = Modifier.height(24.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = error,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun AuthButton(
    text: String,
    onClick: () -> Unit,
    backgroundColor: Color,
    textColor: Color,
    iconRes: Int? = null,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = textColor
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            iconRes?.let {
                Image(
                    painter = painterResource(id = it),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = Modifier.width(12.dp))
            }

            Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}