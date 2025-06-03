package com.example.focuslearnmobile.data.repository

import android.content.Context
import android.content.Intent
import com.focuslearn.mobile.data.api.FocusLearnApi
import com.focuslearn.mobile.data.model.UserDTO
import com.example.focuslearnmobile.data.local.TokenStorage
import com.example.focuslearnmobile.data.model.*
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val api: FocusLearnApi,
    private val tokenStorage: TokenStorage,
    private val context: Context
) {

    private var googleSignInClient: GoogleSignInClient? = null
    private var callbackManager: CallbackManager? = null

    // Ініціалізація Google Sign-In
    fun initializeGoogleSignIn(webClientId: String) {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(context, gso)
    }

    // Ініціалізація Facebook Login
    fun initializeFacebookLogin() {
        callbackManager = CallbackManager.Factory.create()
    }

    // Google Sign-In Intent
    fun getGoogleSignInIntent(): Intent? {
        return googleSignInClient?.signInIntent
    }

    // Обробка результату Google Sign-In
    suspend fun handleGoogleSignInResult(data: Intent?): AuthResult<AuthResponse> = withContext(Dispatchers.IO) {
        try {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)

            if (account?.idToken != null) {
                val authRequest = DirectAuthRequest(
                    provider = "Google",
                    idToken = account.idToken!!,
                    email = account.email ?: "",
                    name = account.displayName ?: "",
                    photoUrl = account.photoUrl?.toString(),
                    providerId = account.id ?: ""
                )

                authenticateWithServer(authRequest)
            } else {
                AuthResult.Error("Google ID token not received")
            }
        } catch (e: ApiException) {
            AuthResult.Error("Google sign-in failed: ${e.localizedMessage}")
        }
    }

    // Facebook Login
    fun loginWithFacebook(
        onSuccess: (AccessToken) -> Unit,
        onError: (String) -> Unit
    ) {
        val loginManager = LoginManager.getInstance()

        loginManager.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult) {
                onSuccess(result.accessToken)
            }

            override fun onCancel() {
                onError("Facebook login cancelled")
            }

            override fun onError(error: FacebookException) {
                onError("Facebook login error: ${error.localizedMessage}")
            }
        })
    }

    // Обробка Facebook callback
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager?.onActivityResult(requestCode, resultCode, data)
    }

    // Аутентифікація з сервером
    private suspend fun authenticateWithServer(request: DirectAuthRequest): AuthResult<AuthResponse> {
        return try {
            val response = api.directAuth(request)
            if (response.isSuccessful) {
                response.body()?.let { apiResponse ->
                    if (apiResponse.success && apiResponse.data != null) {
                        // Зберігаємо токен
                        tokenStorage.saveToken(apiResponse.data.token)

                        // Зберігаємо дані користувача
                        val user = apiResponse.data.user
                        tokenStorage.saveUserData(
                            userId = user.userId,
                            email = user.email,
                            userName = user.userName,
                            profilePhoto = user.profilePhoto
                        )

                        AuthResult.Success(apiResponse.data)
                    } else {
                        AuthResult.Error(apiResponse.message ?: "Authentication failed")
                    }
                } ?: AuthResult.Error("Empty response")
            } else {
                AuthResult.Error("Server error: ${response.code()}")
            }
        } catch (e: Exception) {
            AuthResult.Error("Network error: ${e.localizedMessage}")
        }
    }

    // Отримання поточного користувача
    suspend fun getCurrentUser(): AuthResult<UserDTO> = withContext(Dispatchers.IO) {
        try {
            val token = tokenStorage.getToken().firstOrNull()
            if (!token.isNullOrEmpty()) {
                val response = api.getMyProfile("Bearer $token")
                if (response.isSuccessful) {
                    response.body()?.let { apiResponse ->
                        if (apiResponse.success && apiResponse.data != null) {
                            AuthResult.Success(apiResponse.data)
                        } else {
                            AuthResult.Error(apiResponse.message ?: "Failed to get user")
                        }
                    } ?: AuthResult.Error("Empty response")
                } else {
                    AuthResult.Error("Server error: ${response.code()}")
                }
            } else {
                AuthResult.Error("No token found")
            }
        } catch (e: Exception) {
            AuthResult.Error("Network error: ${e.localizedMessage}")
        }
    }

    // Logout
    suspend fun logout() {
        googleSignInClient?.signOut()
        LoginManager.getInstance().logOut()
        tokenStorage.clearAll()
    }

    // Перевірка авторизації
    fun isAuthenticated(): Flow<Boolean> {
        return tokenStorage.getToken().map { token ->
            !token.isNullOrEmpty()
        }
    }

    // Отримання токена
    fun getToken(): Flow<String?> = tokenStorage.getToken()

    // Отримання ID користувача
    fun getUserId(): Flow<String?> = tokenStorage.getUserId()

    // Отримання email користувача
    fun getUserEmail(): Flow<String?> = tokenStorage.getUserEmail()

    // Отримання імені користувача
    fun getUserName(): Flow<String?> = tokenStorage.getUserName()

    // Клас для результатів операцій
    sealed class AuthResult<out T> {
        data class Success<T>(val data: T) : AuthResult<T>()
        data class Error(val message: String) : AuthResult<Nothing>()
    }
}