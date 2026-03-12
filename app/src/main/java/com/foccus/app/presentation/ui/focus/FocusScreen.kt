package com.foccus.app.presentation.ui.focus

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.foccus.app.presentation.components.CircularTimer
import com.foccus.app.presentation.components.FoccusTopBar
import com.foccus.app.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusScreen(
    onNavigateBack: () -> Unit,
    viewModel: FocusViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDurationPicker by remember { mutableStateOf(false) }

    if (showDurationPicker) {
        DurationPickerDialog(
            currentMinutes = uiState.plannedDurationMinutes,
            onConfirm = { minutes ->
                viewModel.setDuration(minutes)
                showDurationPicker = false
            },
            onDismiss = { showDurationPicker = false }
        )
    }

    Scaffold(
        topBar = {
            FoccusTopBar(
                title = "Sessão de Foco",
                onNavigateBack = if (uiState.state == FocusState.IDLE ||
                    uiState.state == FocusState.FINISHED
                ) onNavigateBack else null
            )
        },
        containerColor = BackgroundDark
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                CircularTimer(
                    progress = uiState.progress,
                    timeText = uiState.timeText,
                    labelText = when (uiState.state) {
                        FocusState.IDLE -> "PRONTO"
                        FocusState.RUNNING -> "FOCANDO"
                        FocusState.PAUSED -> "PAUSADO"
                        FocusState.FINISHED -> "CONCLUÍDO"
                    },
                    isActive = uiState.isActive
                )

                Spacer(modifier = Modifier.height(24.dp))

                AnimatedVisibility(visible = uiState.state == FocusState.IDLE) {
                    DurationSelector(
                        currentMinutes = uiState.plannedDurationMinutes,
                        onClick = { showDurationPicker = true }
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

                when (uiState.state) {
                    FocusState.IDLE -> IdleControls(onStart = { viewModel.startSession() })
                    FocusState.RUNNING -> RunningControls(
                        onPause = { viewModel.pauseSession() },
                        onStop = { viewModel.stopSession() }
                    )
                    FocusState.PAUSED -> PausedControls(
                        onResume = { viewModel.resumeSession() },
                        onStop = { viewModel.stopSession() }
                    )
                    FocusState.FINISHED -> FinishedControls(
                        onBack = onNavigateBack,
                        onRestart = { viewModel.stopSession() }
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                AnimatedVisibility(
                    visible = uiState.state == FocusState.RUNNING ||
                            uiState.state == FocusState.PAUSED
                ) {
                    SessionInfoCard(elapsedMinutes = uiState.elapsedMinutes)
                }
            }
        }
    }
}

@Composable
private fun DurationSelector(
    currentMinutes: Int,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(SurfaceVariantDark)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Timer,
            contentDescription = null,
            tint = OnSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$currentMinutes minutos",
            style = MaterialTheme.typography.titleSmall.copy(
                color = OnSurface,
                fontWeight = FontWeight.Light
            )
        )
        Spacer(modifier = Modifier.width(6.dp))
        Icon(
            imageVector = Icons.Filled.KeyboardArrowDown,
            contentDescription = null,
            tint = OnSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun IdleControls(onStart: () -> Unit) {
    Button(
        onClick = onStart,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(4.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Accent,
            contentColor = BackgroundDark
        )
    ) {
        Icon(
            imageVector = Icons.Filled.PlayArrow,
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "INICIAR FOCO",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Normal
            )
        )
    }
}

@Composable
private fun RunningControls(onPause: () -> Unit, onStop: () -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedButton(
            onClick = onStop,
            modifier = Modifier
                .weight(1f)
                .height(56.dp),
            shape = RoundedCornerShape(4.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = OnSurfaceVariant
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, Outline)
        ) {
            Icon(imageVector = Icons.Filled.Stop, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("PARAR", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Light))
        }

        Button(
            onClick = onPause,
            modifier = Modifier
                .weight(1f)
                .height(56.dp),
            shape = RoundedCornerShape(4.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = SurfaceVariantDark,
                contentColor = OnBackground
            )
        ) {
            Icon(imageVector = Icons.Filled.Pause, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("PAUSAR", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Light))
        }
    }
}

@Composable
private fun PausedControls(onResume: () -> Unit, onStop: () -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedButton(
            onClick = onStop,
            modifier = Modifier
                .weight(1f)
                .height(56.dp),
            shape = RoundedCornerShape(4.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = OnSurfaceVariant),
            border = androidx.compose.foundation.BorderStroke(1.dp, Outline)
        ) {
            Text("ABANDONAR", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Light))
        }

        Button(
            onClick = onResume,
            modifier = Modifier
                .weight(1f)
                .height(56.dp),
            shape = RoundedCornerShape(4.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Accent,
                contentColor = BackgroundDark
            )
        ) {
            Icon(imageVector = Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("RETOMAR", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Normal))
        }
    }
}

@Composable
private fun FinishedControls(onBack: () -> Unit, onRestart: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "SESSÃO CONCLUÍDA",
            style = MaterialTheme.typography.headlineSmall.copy(
                color = OnBackground,
                fontWeight = FontWeight.Light,
                textAlign = TextAlign.Center
            )
        )
        Text(
            text = "Excelente trabalho. Faça uma pausa merecida.",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = OnSurfaceVariant,
                textAlign = TextAlign.Center
            )
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedButton(
                onClick = onRestart,
                modifier = Modifier.weight(1f).height(52.dp),
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Accent),
                border = androidx.compose.foundation.BorderStroke(1.dp, Outline)
            ) { Text("NOVA SESSÃO", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Light)) }

            Button(
                onClick = onBack,
                modifier = Modifier.weight(1f).height(52.dp),
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SurfaceVariantDark)
            ) { Text("VOLTAR", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Light)) }
        }
    }
}

@Composable
private fun SessionInfoCard(elapsedMinutes: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(SurfaceDark)
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Filled.AccessTime,
                contentDescription = null,
                tint = OnSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Em foco há $elapsedMinutes min",
                style = MaterialTheme.typography.bodyMedium.copy(color = OnSurface)
            )
        }
    }
}

@Composable
private fun DurationPickerDialog(
    currentMinutes: Int,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val options = listOf(10, 15, 20, 25, 30, 45, 60, 90)
    var selected by remember { mutableIntStateOf(currentMinutes) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        shape = RoundedCornerShape(4.dp),
        title = {
            Text(
                "Duração da sessão",
                style = MaterialTheme.typography.titleLarge.copy(
                    color = OnBackground,
                    fontWeight = FontWeight.Light
                )
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                options.chunked(4).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { minutes ->
                            val isSelected = minutes == selected
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        if (isSelected) Accent else SurfaceVariantDark
                                    )
                                    .clickable { selected = minutes }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${minutes}m",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        color = if (isSelected) BackgroundDark else OnSurface,
                                        fontWeight = if (isSelected) FontWeight.Normal else FontWeight.Light
                                    )
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selected) },
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Accent)
            ) {
                Text("CONFIRMAR", color = BackgroundDark, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Light))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCELAR", color = OnSurfaceVariant, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Light))
            }
        }
    )
}
