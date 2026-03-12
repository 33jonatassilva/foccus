package com.foccus.app.presentation.ui.settings

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.foccus.app.presentation.components.FoccusTopBar
import com.foccus.app.presentation.theme.*
import kotlinx.coroutines.suspendCancellableCoroutine

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(Unit) {
        viewModel.refreshPermissions()
    }

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.refreshPermissions()
            suspendCancellableCoroutine<Unit> { }
        }
    }

    Scaffold(
        topBar = { FoccusTopBar(title = "Configurações", onNavigateBack = onNavigateBack) },
        containerColor = BackgroundDark
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            SettingsSection(title = "SESSÃO DE FOCO") {
                DurationSliderItem(
                    label = "Duração do Foco",
                    value = uiState.focusDuration,
                    range = 5..120,
                    step = 5,
                    unit = "min",
                    onValueChange = { viewModel.setFocusDuration(it) }
                )
                HorizontalDivider(color = OutlineVariant.copy(alpha = 0.5f))
                DurationSliderItem(
                    label = "Duração da Pausa",
                    value = uiState.breakDuration,
                    range = 1..30,
                    step = 1,
                    unit = "min",
                    onValueChange = { viewModel.setBreakDuration(it) }
                )
            }

            SettingsSection(title = "CONTROLES") {
                SwitchSettingItem(
                    label = "Bloqueio de Apps",
                    description = "Bloquear apps de distração durante o foco",
                    icon = Icons.Filled.Block,
                    isChecked = uiState.blockingEnabled,
                    onCheckedChange = { viewModel.toggleBlocking(it) }
                )
                HorizontalDivider(color = OutlineVariant.copy(alpha = 0.5f))
                SwitchSettingItem(
                    label = "Escala de Cinza",
                    description = "Converter cores vivas para cinza em todo o aparelho",
                    icon = Icons.Outlined.Tonality,
                    isChecked = uiState.grayscaleEnabled,
                    onCheckedChange = { viewModel.toggleGrayscale(it) }
                )
                HorizontalDivider(color = OutlineVariant.copy(alpha = 0.5f))
                SwitchSettingItem(
                    label = "YouTube Shorts",
                    description = "Bloquear Shorts mesmo com YouTube liberado",
                    icon = Icons.Filled.VideoLibrary,
                    isChecked = uiState.blockShortsEnabled,
                    onCheckedChange = { viewModel.toggleBlockShorts(it) }
                )
                HorizontalDivider(color = OutlineVariant.copy(alpha = 0.5f))
                SwitchSettingItem(
                    label = "Notificações",
                    description = "Alertas e lembretes de sessão",
                    icon = Icons.Outlined.Notifications,
                    isChecked = uiState.notificationsEnabled,
                    onCheckedChange = { viewModel.toggleNotifications(it) }
                )
            }

            SettingsSection(title = "PERMISSÕES") {
                PermissionItem(
                    label = "Serviço de Acessibilidade",
                    description = "Necessário para bloquear apps",
                    isGranted = uiState.hasAccessibilityEnabled,
                    onClick = {
                        context.startActivity(
                            Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                        )
                    }
                )
                HorizontalDivider(color = OutlineVariant.copy(alpha = 0.5f))
                PermissionItem(
                    label = "Overlay (Sobrepor apps)",
                    description = "Necessário para alertas de bloqueio",
                    isGranted = uiState.hasOverlayPermission,
                    onClick = {
                        context.startActivity(
                            Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:${context.packageName}")
                            )
                        )
                    }
                )
                HorizontalDivider(color = OutlineVariant.copy(alpha = 0.5f))
                PermissionItem(
                    label = "Estatísticas de uso",
                    description = "Monitorar apps em primeiro plano",
                    isGranted = uiState.hasUsageStatsPermission,
                    onClick = {
                        context.startActivity(
                            Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                        )
                    }
                )
            }

            SettingsSection(title = "SOBRE") {
                InfoItem(label = "Versão", value = "1.0.0")
                HorizontalDivider(color = OutlineVariant.copy(alpha = 0.5f))
                InfoItem(label = "Pacote", value = context.packageName)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (uiState.showGrayscaleGuide) {
        GrayscaleGuideDialog(
            onOpenSettings = {
                viewModel.openColorCorrectionSettings(context)
                viewModel.dismissGrayscaleGuide()
            },
            onDismiss = { viewModel.dismissGrayscaleGuide() }
        )
    }
}

@Composable
private fun GrayscaleGuideDialog(
    onOpenSettings: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        shape = RoundedCornerShape(4.dp),
        title = {
            Text(
                text = "ESCALA DE CINZA",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = OnBackground,
                    fontWeight = FontWeight.Light,
                    letterSpacing = 2.sp
                )
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "A escala de cinza converte apenas cores vivas para cinza, " +
                            "mantendo branco, preto e cinza intactos. " +
                            "Essa função usa a Correção de Cor nativa do Android.",
                    style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceVariant)
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    GuideStep(number = "1", text = "Toque em \"Abrir configurações\" abaixo")
                    GuideStep(number = "2", text = "Ative \"Correção de cor\" ou \"Correção de cores\"")
                    GuideStep(number = "3", text = "Selecione \"Escala de cinza\" ou \"Tons de cinza\"")
                }
                Text(
                    text = "Após ativar, volte ao Foccus. O estado será sincronizado automaticamente.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = OnSurfaceVariant.copy(alpha = 0.7f)
                    )
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onOpenSettings) {
                Text("ABRIR CONFIGURAÇÕES", color = Accent, fontWeight = FontWeight.Normal)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("FECHAR", color = OnSurfaceVariant)
            }
        }
    )
}

@Composable
private fun GuideStep(number: String, text: String) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(SurfaceVariantDark),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Accent,
                    fontWeight = FontWeight.Normal
                )
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(color = OnBackground),
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(
                color = OnSurfaceVariant,
                letterSpacing = 3.sp
            ),
            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .background(SurfaceDark)
        ) {
            Column(content = content)
        }
    }
}

@Composable
private fun SwitchSettingItem(
    label: String,
    description: String,
    icon: ImageVector,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!isChecked) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(SurfaceVariantDark),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = OnSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall.copy(
                    color = OnBackground,
                    fontWeight = FontWeight.Light
                )
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceVariant)
            )
        }
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = BackgroundDark,
                checkedTrackColor = Accent,
                uncheckedTrackColor = SurfaceVariantDark
            )
        )
    }
}

@Composable
private fun DurationSliderItem(
    label: String,
    value: Int,
    range: IntRange,
    step: Int,
    unit: String,
    onValueChange: (Int) -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall.copy(
                    color = OnBackground,
                    fontWeight = FontWeight.Light
                )
            )
            Text(
                text = "$value $unit",
                style = MaterialTheme.typography.titleSmall.copy(
                    color = Accent,
                    fontWeight = FontWeight.Normal
                )
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = range.first.toFloat()..range.last.toFloat(),
            steps = (range.last - range.first) / step - 1,
            colors = SliderDefaults.colors(
                thumbColor = Accent,
                activeTrackColor = Accent,
                inactiveTrackColor = SurfaceVariantDark
            )
        )
    }
}

@Composable
private fun PermissionItem(
    label: String,
    description: String,
    isGranted: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall.copy(
                    color = OnBackground,
                    fontWeight = FontWeight.Light
                )
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceVariant)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(2.dp))
                .background(
                    if (isGranted) Accent.copy(alpha = 0.12f)
                    else SurfaceVariantDark
                )
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                text = if (isGranted) "ATIVO" else "PENDENTE",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = if (isGranted) Accent else OnSurfaceVariant,
                    fontWeight = FontWeight.Normal
                )
            )
        }
    }
}

@Composable
private fun InfoItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall.copy(
                color = OnBackground,
                fontWeight = FontWeight.Light
            )
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceVariant)
        )
    }
}
