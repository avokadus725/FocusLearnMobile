// app/src/main/java/com/example/focuslearnmobile/ui/profile/ProfileScreen.kt
package com.example.focuslearnmobile.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.focuslearnmobile.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showEditDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Заголовок профілю з градієнтом
            ProfileHeader(
                userName = uiState.user?.userName ?: "",
                userEmail = uiState.user?.email ?: "",
                profilePhotoUrl = uiState.user?.profilePhoto,
                onEditClick = { showEditDialog = true },
                isLoading = uiState.isLoading
            )
        }

        item {
            // Інформація про користувача
            UserInfoCard(
                user = uiState.user,
                isLoading = uiState.isLoading
            )
        }

        item {
            // Дії профілю
            ProfileActionsCard(
                onRefreshProfile = { viewModel.refreshProfile() },
                isLoading = uiState.isLoading
            )
        }

        // Показуємо помилки
        uiState.error?.let { error ->
            item {
                ErrorAlert(
                    error = error,
                    onDismiss = { viewModel.clearError() }
                )
            }
        }

        // Показуємо успішне повідомлення
        uiState.successMessage?.let { message ->
            item {
                SuccessAlert(
                    message = message,
                    onDismiss = { viewModel.clearSuccessMessage() }
                )
            }
        }
    }

    // Діалог редагування профілю
    if (showEditDialog) {
        EditProfileDialog(
            user = uiState.user,
            onSave = { name, photo, language ->
                viewModel.updateProfile(name, photo, language)
                showEditDialog = false
            },
            onDismiss = { showEditDialog = false },
            isLoading = uiState.isUpdating
        )
    }
}

@Composable
fun ProfileHeader(
    userName: String,
    userEmail: String,
    onEditClick: () -> Unit,
    isLoading: Boolean,
    profilePhotoUrl: String? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.tertiary
                        )
                    )
                )
                .padding(24.dp)
        ) {
            // Кнопка редагування в правому верхньому куті
            IconButton(
                onClick = onEditClick,
                enabled = !isLoading,
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = stringResource(R.string.edit_profile),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }

            // Основний контент по центру
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp), // Відступ від кнопки редагування
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Аватар/Фото профілю
                ProfileAvatar(
                    profilePhotoUrl = profilePhotoUrl,
                    userName = userName,
                    size = 100.dp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Інформація користувача
                if (isLoading) {
                    // Показуємо завантаження
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .height(28.dp)
                                .width(150.dp)
                                .background(
                                    MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f),
                                    RoundedCornerShape(4.dp)
                                )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .height(20.dp)
                                .width(200.dp)
                                .background(
                                    MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f),
                                    RoundedCornerShape(4.dp)
                                )
                        )
                    }
                } else {
                    Text(
                        text = userName.ifEmpty { "Користувач" },
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = userEmail,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileAvatar(
    profilePhotoUrl: String?,
    userName: String,
    size: Dp = 80.dp
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)),
        contentAlignment = Alignment.Center
    ) {
        if (!profilePhotoUrl.isNullOrBlank()) {
            var isLoading by remember { mutableStateOf(true) }
            var hasError by remember { mutableStateOf(false) }

            if (hasError) {
                // Показуємо fallback при помилці
                if (userName.isNotBlank()) {
                    UserInitials(userName, size)
                } else {
                    DefaultAvatarIcon(size)
                }
            } else {
                AsyncImage(
                    model = profilePhotoUrl,
                    contentDescription = "Profile photo",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop,
                    onSuccess = {
                        isLoading = false
                        hasError = false
                    },
                    onError = {
                        isLoading = false
                        hasError = true
                    },
                    onLoading = {
                        isLoading = true
                        hasError = false
                    }
                )

                // Показуємо індикатор завантаження поверх зображення
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(size * 0.3f),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    }
                }
            }
        } else {
            // Якщо немає URL фото, показуємо іконку або ініціали
            if (userName.isNotBlank()) {
                UserInitials(userName, size)
            } else {
                DefaultAvatarIcon(size)
            }
        }
    }
}

@Composable
fun DefaultAvatarIcon(size: Dp) {
    Icon(
        imageVector = Icons.Default.Person,
        contentDescription = null,
        modifier = Modifier.size(size * 0.5f),
        tint = MaterialTheme.colorScheme.onPrimary
    )
}

@Composable
fun UserInitials(userName: String, size: Dp) {
    val initials = userName.split(" ")
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .take(2)
        .joinToString("")

    if (initials.isNotBlank()) {
        Text(
            text = initials,
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = (size.value * 0.3f).sp
        )
    } else {
        DefaultAvatarIcon(size)
    }
}

@Composable
fun UserInfoCard(
    user: com.focuslearn.mobile.data.model.UserDTO?,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(R.string.profile_information),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (isLoading) {
                // Простий індикатор завантаження
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                user?.let {
                    ProfileInfoItem(
                        label = stringResource(R.string.user_id),
                        value = it.userId.toString(),
                        icon = Icons.Default.Tag
                    )

                    ProfileInfoItem(
                        label = stringResource(R.string.email),
                        value = it.email,
                        icon = Icons.Default.Email
                    )

                    ProfileInfoItem(
                        label = stringResource(R.string.language),
                        value = when (it.language) {
                            "uk" -> "Українська"
                            "en" -> "English"
                            else -> it.language ?: "Не вказано"
                        },
                        icon = Icons.Default.Language
                    )

                    ProfileInfoItem(
                        label = stringResource(R.string.role),
                        value = it.role ?: "Студент",
                        icon = Icons.Default.Person
                    )

                    ProfileInfoItem(
                        label = stringResource(R.string.status),
                        value = it.profileStatus ?: "Активний",
                        icon = Icons.Default.CheckCircle
                    )
                } ?: run {
                    Text(
                        text = stringResource(R.string.no_profile_data),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileInfoItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun ProfileActionsCard(
    onRefreshProfile: () -> Unit,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.actions),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Button(
                onClick = onRefreshProfile,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.refresh_profile))
            }
        }
    }
}

@Composable
fun ErrorAlert(
    error: String,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer
            )

            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.weight(1f)
            )

            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Закрити",
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

@Composable
fun SuccessAlert(
    message: String,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF0FDF4)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF15803D)
            )

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF15803D),
                modifier = Modifier.weight(1f)
            )

            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Закрити",
                    tint = Color(0xFF15803D)
                )
            }
        }
    }

    @Composable
    fun IoTSyncSettingsCard(
        viewModel: ProfileViewModel = hiltViewModel()
    ) {
        var iotEnabled by remember { mutableStateOf(false) }
        var showIoTDialog by remember { mutableStateOf(false) }

        // Завантажуємо налаштування при старті
        LaunchedEffect(Unit) {
            // iotEnabled = viewModel.getIoTSettings()
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeviceHub,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "IoT Синхронізація",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Основний перемикач IoT
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Використовувати IoT пристрій",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (iotEnabled) "Дані записуються з IoT пристрою" else "Дані записуються з додатку",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Switch(
                        checked = iotEnabled,
                        onCheckedChange = { newValue ->
                            if (newValue) {
                                showIoTDialog = true
                            } else {
                                iotEnabled = false
                                // viewModel.saveIoTSettings(false)
                            }
                        }
                    )
                }

                // Додаткова інформація
                if (iotEnabled) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Мобільний таймер працює тільки для відображення. Дані записуються автоматично з IoT пристрою.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // Діалог підтвердження
        if (showIoTDialog) {
            AlertDialog(
                onDismissRequest = { showIoTDialog = false },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text("Увімкнути IoT синхронізацію?")
                    }
                },
                text = {
                    Text(
                        "Це вимкне запис даних з мобільного додатку. Всі дані будуть записуватися автоматично з вашого IoT пристрою.\n\nПереконайтеся, що ваш пристрій правильно налаштований та підключений."
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            iotEnabled = true
                            showIoTDialog = false
                            // viewModel.saveIoTSettings(true)
                        }
                    ) {
                        Text("Увімкнути")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showIoTDialog = false }
                    ) {
                        Text("Скасувати")
                    }
                }
            )
        }
    }
}