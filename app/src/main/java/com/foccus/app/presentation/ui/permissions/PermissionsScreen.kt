package com.foccus.app.presentation.ui.permissions

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
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.foccus.app.presentation.theme.*
import kotlinx.coroutines.suspendCancellableCoroutine

@Composable
fun PermissionsScreen(
    onContinue: () -> Unit,
    viewModel: PermissionsViewModel = hiltViewModel()
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
        containerColor = BackgroundDark,
        bottomBar = {
            Button(
                onClick = {
                    viewModel.completeOnboarding()
                    onContinue()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp)
                    .height(52.dp),
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Accent,
                    contentColor = BackgroundDark
                )
            ) {
                Text(
                    text = "CONTINUAR",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Normal
                    )
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Icon(
                imageVector = Icons.Filled.Shield,
                contentDescription = null,
                tint = Accent,
                modifier = Modifier.size(64.dp)
            )

            Text(
                text = "PERMISSÕES NECESSÁRIAS",
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = OnBackground,
                    fontWeight = FontWeight.Light,
                    textAlign = TextAlign.Center
                )
            )

            Text(
                text = "Para bloquear apps e exibir alertas, ative as permissões abaixo. Pode configurá-las depois em Configurações.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = OnSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp))
                    .background(SurfaceDark)
            ) {
                Column {
                    PermissionRow(
                        label = "Serviço de Acessibilidade",
                        description = "Necessário para bloquear apps",
                        isGranted = uiState.hasAccessibilityEnabled,
                        onClick = {
                            context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                        }
                    )
                    HorizontalDivider(color = OutlineVariant.copy(alpha = 0.5f))
                    PermissionRow(
                        label = "Overlay (Sobrepor apps)",
                        description = "Necessário para exibir alertas de bloqueio",
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
                    PermissionRow(
                        label = "Estatísticas de uso",
                        description = "Monitorar apps em primeiro plano",
                        isGranted = uiState.hasUsageStatsPermission,
                        onClick = {
                            context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PermissionRow(
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
