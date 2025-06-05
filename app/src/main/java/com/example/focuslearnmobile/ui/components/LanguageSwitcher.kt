// app/src/main/java/com/example/focuslearnmobile/ui/components/LanguageSwitcher.kt
package com.example.focuslearnmobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.focuslearnmobile.R
import com.example.focuslearnmobile.data.local.LanguageManager
// ViewModel для управління мовою
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
// Дані для мов
data class Language(
    val code: String,
    val name: String,
    val nativeName: String,
    val flag: String = ""
)



@HiltViewModel
class LanguageSwitcherViewModel @Inject constructor(
    private val languageManager: LanguageManager
) : ViewModel() {

    val currentLanguage: StateFlow<String> = languageManager.currentLanguage.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LanguageManager.LANGUAGE_ENGLISH
    )

    fun changeLanguage(languageCode: String, onLanguageChanged: (() -> Unit)? = null) {
        viewModelScope.launch {
            languageManager.setLanguage(languageCode)
            // Викликаємо callback для перезапуску Activity
            onLanguageChanged?.invoke()
        }
    }

    fun getSupportedLanguages(): List<Language> {
        return listOf(
            Language(
                code = LanguageManager.LANGUAGE_ENGLISH,
                name = "English",
                nativeName = "English",
                flag = "🇺🇸"
            ),
            Language(
                code = LanguageManager.LANGUAGE_UKRAINIAN,
                name = "Ukrainian",
                nativeName = "Українська",
                flag = "🇺🇦"
            )
        )
    }
}

// Кнопка в TopAppBar для переключення мови
@Composable
fun LanguageSwitcherButton(
    onLanguageChanged: (() -> Unit)? = null,
    viewModel: LanguageSwitcherViewModel = hiltViewModel()
) {
    var showDialog by remember { mutableStateOf(false) }
    val currentLanguage by viewModel.currentLanguage.collectAsState()

    IconButton(
        onClick = { showDialog = true }
    ) {
        Icon(
            imageVector = Icons.Default.Language,
            contentDescription = stringResource(R.string.language),
            tint = MaterialTheme.colorScheme.onSurface
        )
    }

    if (showDialog) {
        LanguageSelectionDialog(
            currentLanguage = currentLanguage,
            languages = viewModel.getSupportedLanguages(),
            onLanguageSelected = { languageCode ->
                viewModel.changeLanguage(languageCode, onLanguageChanged)
                showDialog = false
            },
            onDismiss = { showDialog = false }
        )
    }
}

// Діалог для вибору мови
@Composable
fun LanguageSelectionDialog(
    currentLanguage: String,
    languages: List<Language>,
    onLanguageSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                // Заголовок
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = stringResource(R.string.select_language),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Список мов
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(languages) { language ->
                        LanguageItem(
                            language = language,
                            isSelected = currentLanguage == language.code,
                            onClick = { onLanguageSelected(language.code) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Кнопка скасування
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            }
        }
    }
}

// Елемент списку мови
@Composable
fun LanguageItem(
    language: Language,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .background(
                if (isSelected) {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                } else {
                    Color.Transparent
                }
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Прапор (емодзі)
        Text(
            text = language.flag,
            style = MaterialTheme.typography.titleLarge
        )

        // Назви мови
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = language.nativeName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (language.name != language.nativeName) {
                Text(
                    text = language.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Іконка вибору
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// Компактна кнопка переключення (показує поточну мову)
@Composable
fun CompactLanguageSwitcher(
    viewModel: LanguageSwitcherViewModel = hiltViewModel()
) {
    var showDialog by remember { mutableStateOf(false) }
    val currentLanguage by viewModel.currentLanguage.collectAsState()
    val languages = viewModel.getSupportedLanguages()
    val currentLanguageData = languages.find { it.code == currentLanguage }

    OutlinedButton(
        onClick = { showDialog = true },
        modifier = Modifier.height(36.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
        border = ButtonDefaults.outlinedButtonBorder.copy(
            width = 1.dp
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = currentLanguageData?.flag ?: "🌐",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = when (currentLanguage) {
                    LanguageManager.LANGUAGE_UKRAINIAN -> "УКР"
                    else -> "ENG"
                },
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }

    if (showDialog) {
        LanguageSelectionDialog(
            currentLanguage = currentLanguage,
            languages = languages,
            onLanguageSelected = { languageCode ->
                viewModel.changeLanguage(languageCode)
                showDialog = false
            },
            onDismiss = { showDialog = false }
        )
    }
}