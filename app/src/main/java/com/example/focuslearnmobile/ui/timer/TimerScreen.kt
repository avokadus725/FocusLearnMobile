// app/src/main/java/com/example/focuslearnmobile/ui/timer/TimerScreen.kt
package com.example.focuslearnmobile.ui.timer

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.focuslearnmobile.R
import com.example.focuslearnmobile.data.model.TimerPhase

@Composable
fun TimerScreen(
    viewModel: TimerViewModel = hiltViewModel()
) {
    val timerState by viewModel.timerState.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Показуємо IoT повідомлення
        uiState.iotMessage?.let { message ->
            IoTMessageCard(
                message = message,
                onDismiss = { viewModel.clearIoTMessage() }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (timerState.isActive) {
            // Активна сесія - показуємо таймер
            ActiveTimerContent(
                timerState = timerState,
                onStop = viewModel::stopSession,
                isLoading = uiState.isLoading
            )
        } else {
            // Немає активної сесії - показуємо вибір методики
            MethodSelectionScreen(
                onMethodSelected = { method ->
                    viewModel.startSession(method)
                }
            )
        }

        // Показуємо помилки
        uiState.error?.let { error ->
            LaunchedEffect(error) {
                viewModel.clearError()
            }
        }
    }
}

@Composable
private fun IoTMessageCard(
    message: String,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE6F4EA) // Світло-зелений для IoT повідомлень
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.DeviceHub,
                contentDescription = null,
                tint = Color(0xFF2E7D32),
                modifier = Modifier.size(20.dp)
            )

            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF2E7D32),
                modifier = Modifier.weight(1f)
            )

            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Закрити",
                    tint = Color(0xFF2E7D32),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun ActiveTimerContent(
    timerState: com.example.focuslearnmobile.data.model.TimerState,
    onStop: () -> Unit,
    isLoading: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Інформація про поточну сесію
        SessionInfoCard(timerState)

        // Круговий таймер
        CircularTimer(
            remainingSeconds = timerState.remainingSeconds,
            totalSeconds = timerState.totalPhaseSeconds,
            isWork = timerState.currentPhase == TimerPhase.WORK
        )

        // Час що залишився
        TimeDisplay(
            remainingSeconds = timerState.remainingSeconds
        )

        // Кнопки управління
        TimerControls(
            onStop = onStop,
            isLoading = isLoading
        )

        // IoT інформація
        IoTInfoCard()
    }
}

@Composable
private fun SessionInfoCard(timerState: com.example.focuslearnmobile.data.model.TimerState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = timerState.methodTitle,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PhaseChip(
                    text = when (timerState.currentPhase) {
                        TimerPhase.WORK -> stringResource(R.string.work_phase)
                        TimerPhase.BREAK -> stringResource(R.string.break_phase)
                    },
                    isWork = timerState.currentPhase == TimerPhase.WORK
                )

                Text(
                    text = "•",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = stringResource(R.string.cycle_number, timerState.currentCycle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PhaseChip(
    text: String,
    isWork: Boolean
) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = if (isWork) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.secondaryContainer
        },
        contentColor = if (isWork) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSecondaryContainer
        }
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun CircularTimer(
    remainingSeconds: Int,
    totalSeconds: Int,
    isWork: Boolean
) {
    val progress = if (totalSeconds > 0) {
        (totalSeconds - remainingSeconds).toFloat() / totalSeconds.toFloat()
    } else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 300),
        label = "timer_progress"
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant

    Box(
        modifier = Modifier.size(280.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val strokeWidth = 12.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2f
            val center = Offset(size.width / 2f, size.height / 2f)

            // Фон кола
            drawCircle(
                color = surfaceVariant,
                radius = radius,
                center = center,
                style = Stroke(width = strokeWidth)
            )

            // Прогрес
            if (animatedProgress > 0f) {
                drawArc(
                    color = if (isWork) primaryColor else secondaryColor,
                    startAngle = -90f,
                    sweepAngle = 360f * animatedProgress,
                    useCenter = false,
                    topLeft = Offset(
                        center.x - radius,
                        center.y - radius
                    ),
                    size = Size(radius * 2f, radius * 2f),
                    style = Stroke(
                        width = strokeWidth,
                        cap = StrokeCap.Round
                    )
                )
            }
        }

        // Іконка в центрі
        val icon = if (isWork) Icons.Default.Work else Icons.Default.Coffee

        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = if (isWork) primaryColor else secondaryColor
        )
    }
}

@Composable
private fun TimeDisplay(
    remainingSeconds: Int
) {
    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    val timeText = String.format("%02d:%02d", minutes, seconds)

    Text(
        text = timeText,
        style = MaterialTheme.typography.displayLarge.copy(
            fontSize = 56.sp,
            fontWeight = FontWeight.Light
        ),
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
private fun TimerControls(
    onStop: () -> Unit,
    isLoading: Boolean
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Тільки кнопка зупинки - всі інші дії через IoT
        FloatingActionButton(
            onClick = { if (!isLoading) onStop() },
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Stop,
                    contentDescription = stringResource(R.string.stop_session)
                )
            }
        }
    }
}

@Composable
private fun IoTInfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE3F2FD) // Світло-синій для IoT інформації
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = Color(0xFF1565C0),
                modifier = Modifier.size(20.dp)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "IoT режим активний",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF1565C0),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Дані автоматично записуються через підключений пристрій",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF1565C0)
                )
            }

            Icon(
                imageVector = Icons.Default.DeviceHub,
                contentDescription = null,
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}