// app/src/main/java/com/example/focuslearnmobile/ui/profile/EditProfileDialog.kt
package com.example.focuslearnmobile.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.focuslearnmobile.R
import com.example.focuslearnmobile.data.local.LanguageManager
import com.focuslearn.mobile.data.model.UserDTO

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileDialog(
    user: UserDTO?,
    onSave: (String, String?, String?) -> Unit,
    onDismiss: () -> Unit,
    isLoading: Boolean = false
) {
    var userName by remember { mutableStateOf(user?.userName ?: "") }
    var profilePhoto by remember { mutableStateOf(user?.profilePhoto ?: "") }
    var selectedLanguage by remember { mutableStateOf(user?.language ?: LanguageManager.LANGUAGE_ENGLISH) }
    var showLanguageDropdown by remember { mutableStateOf(false) }

    val keyboardController = LocalSoftwareKeyboardController.current

    val languages = listOf(
        LanguageManager.LANGUAGE_ENGLISH to "English",
        LanguageManager.LANGUAGE_UKRAINIAN to "Українська"
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Заголовок
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = stringResource(R.string.edit_profile),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Поле імені користувача
                OutlinedTextField(
                    value = userName,
                    onValueChange = { userName = it },
                    label = { Text(stringResource(R.string.user_name)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next
                    ),
                    singleLine = true
                )

                // Поле URL фото профілю
                OutlinedTextField(
                    value = profilePhoto,
                    onValueChange = { profilePhoto = it },
                    label = { Text(stringResource(R.string.profile_photo_url)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = null
                        )
                    },
                    placeholder = { Text("https://example.com/photo.jpg") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Uri,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { keyboardController?.hide() }
                    ),
                    singleLine = true
                )

                // Вибір мови
                ExposedDropdownMenuBox(
                    expanded = showLanguageDropdown,
                    onExpandedChange = { showLanguageDropdown = !showLanguageDropdown && !isLoading }
                ) {
                    OutlinedTextField(
                        value = languages.find { it.first == selectedLanguage }?.second ?: selectedLanguage,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text(stringResource(R.string.language)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = null
                            )
                        },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = showLanguageDropdown)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        enabled = !isLoading,
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )

                    ExposedDropdownMenu(
                        expanded = showLanguageDropdown,
                        onDismissRequest = { showLanguageDropdown = false }
                    ) {
                        languages.forEach { (code, name) ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = when (code) {
                                                LanguageManager.LANGUAGE_UKRAINIAN -> "🇺🇦"
                                                else -> "🇺🇸"
                                            }
                                        )
                                        Text(name)
                                    }
                                },
                                onClick = {
                                    selectedLanguage = code
                                    showLanguageDropdown = false
                                },
                                leadingIcon = if (selectedLanguage == code) {
                                    {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                } else null
                            )
                        }
                    }
                }

                // Інформаційне повідомлення
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = stringResource(R.string.edit_profile_note),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Кнопки дій
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading
                    ) {
                        Text(stringResource(R.string.cancel))
                    }

                    Button(
                        onClick = {
                            val photoUrl = profilePhoto.takeIf { it.isNotBlank() }
                            onSave(userName, photoUrl, selectedLanguage)
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading && userName.isNotBlank()
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text(stringResource(R.string.save))
                        }
                    }
                }
            }
        }
    }
}