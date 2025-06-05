// app/src/main/java/com/example/focuslearnmobile/ui/components/FocusLearnComponents.kt
package com.example.focuslearnmobile.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.focuslearnmobile.ui.theme.SuccessColor
import com.example.focuslearnmobile.ui.theme.White

// Картка з градієнтним заголовком (як у веб-версії)
@Composable
fun GradientHeaderCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column {
            // Градієнтний заголовок
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.tertiary
                            )
                        )
                    )
                    .padding(16.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            // Контент
            Column(
                modifier = Modifier.padding(16.dp),
                content = content
            )
        }
    }
}

// Кнопка з ефектами як у веб-версії
@Composable
fun FocusLearnButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    icon: ImageVector? = null,
    text: String,
    variant: ButtonVariant = ButtonVariant.Primary
) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(100),
        label = "button_scale"
    )

    val colors = when (variant) {
        ButtonVariant.Primary -> ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
        ButtonVariant.Secondary -> ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
        ButtonVariant.Success -> ButtonDefaults.buttonColors(
            containerColor = SuccessColor,
            contentColor = White
        )
        ButtonVariant.Warning -> ButtonDefaults.buttonColors(
            containerColor = Color(0xFFF59E0B),
            contentColor = Color.White
        )
        ButtonVariant.Danger -> ButtonDefaults.buttonColors(
            containerColor = Color(0xFFEF4444),
            contentColor = Color.White
        )
    }

    Button(
        onClick = {
            if (!loading) {
                isPressed = true
                onClick()
                isPressed = false
            }
        },
        modifier = modifier.scale(scale),
        enabled = enabled && !loading,
        shape = RoundedCornerShape(12.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp,
            disabledElevation = 0.dp
        ),
        colors = colors,
        contentPadding = PaddingValues(
            horizontal = 20.dp,
            vertical = 12.dp
        )
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                icon?.let {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

enum class ButtonVariant {
    Primary, Secondary, Success, Warning, Danger
}

// Outline кнопка
@Composable
fun FocusLearnOutlineButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    text: String
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        border = ButtonDefaults.outlinedButtonBorder.copy(
            width = 1.5.dp
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.primary
        ),
        contentPadding = PaddingValues(
            horizontal = 20.dp,
            vertical = 12.dp
        )
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// Іконка в колі з фоном (як у веб-версії)
@Composable
fun CircularIcon(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.primaryContainer,
    iconColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    size: Int = 48
) {
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(RoundedCornerShape(50))
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size((size * 0.5).dp)
        )
    }
}

// Алерт компонент як у веб-версії
@Composable
fun FocusLearnAlert(
    title: String? = null,
    message: String,
    type: AlertType = AlertType.Info,
    modifier: Modifier = Modifier,
    onDismiss: (() -> Unit)? = null,
    icon: ImageVector? = null
) {
    val colors = when (type) {
        AlertType.Success -> Triple(
            Color(0xFFF0FDF4), // background
            Color(0xFF15803D), // content
            Color(0xFF22C55E)  // border
        )
        AlertType.Warning -> Triple(
            Color(0xFFFFFBEB),
            Color(0xFF92400E),
            Color(0xFFF59E0B)
        )
        AlertType.Danger -> Triple(
            Color(0xFFFEF2F2),
            Color(0xFF991B1B),
            Color(0xFFEF4444)
        )
        AlertType.Info -> Triple(
            Color(0xFFEFF6FF),
            Color(0xFF1E40AF),
            Color(0xFF3B82F6)
        )
    }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.first
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = colors.third.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Іконка
            CircularIcon(
                icon = icon ?: when (type) {
                    AlertType.Success -> androidx.compose.material.icons.Icons.Default.CheckCircle
                    AlertType.Warning -> androidx.compose.material.icons.Icons.Default.Warning
                    AlertType.Danger -> androidx.compose.material.icons.Icons.Default.Error
                    AlertType.Info -> androidx.compose.material.icons.Icons.Default.Info
                },
                backgroundColor = colors.third.copy(alpha = 0.2f),
                iconColor = colors.second,
                size = 24
            )

            // Контент
            Column(
                modifier = Modifier.weight(1f)
            ) {
                title?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.titleSmall,
                        color = colors.second,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.second.copy(alpha = 0.9f)
                )
            }

            // Кнопка закриття
            onDismiss?.let {
                IconButton(
                    onClick = it,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Close,
                        contentDescription = "Close",
                        tint = colors.second.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

enum class AlertType {
    Success, Warning, Danger, Info
}

// Loading State компонент
@Composable
fun LoadingState(
    modifier: Modifier = Modifier,
    message: String = "Loading..."
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 3.dp
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Empty State компонент
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    action: @Composable (() -> Unit)? = null
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularIcon(
                icon = icon,
                size = 64,
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                iconColor = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            action?.invoke()
        }
    }
}

// Статистична картка
@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CircularIcon(
                icon = icon,
                backgroundColor = contentColor.copy(alpha = 0.2f),
                iconColor = contentColor,
                size = 40
            )

            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                color = contentColor,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = contentColor.copy(alpha = 0.8f)
            )
        }
    }
}