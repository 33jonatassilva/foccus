package com.foccus.app.presentation.ui.home

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.foccus.app.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToFocus: () -> Unit,
    onNavigateToBlockList: () -> Unit,
    onNavigateToStats: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.reapplyGrayscaleIfNeeded()
    }

    Scaffold(
        containerColor = BackgroundDark
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            HomeHeader(onNavigateToSettings = onNavigateToSettings)

            Spacer(modifier = Modifier.height(24.dp))

            QuickStatsSection(
                focusMinutes = uiState.todayFocusMinutes,
                sessionsToday = uiState.completedSessionsToday,
                blockedApps = uiState.blockedAppsCount,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            FocusStartSection(
                hasActiveSession = uiState.hasActiveSession,
                onStartFocus = onNavigateToFocus,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            QuickTogglesSection(
                isBlockingEnabled = uiState.isBlockingEnabled,
                isGrayscaleEnabled = uiState.isGrayscaleEnabled,
                isShortsBlockingEnabled = uiState.isShortsBlockingEnabled,
                onToggleBlocking = { viewModel.toggleBlocking(it) },
                onToggleGrayscale = {
                    val applied = viewModel.toggleGrayscale(it)
                    if (!applied) {
                        onNavigateToSettings()
                    }
                },
                onToggleShortsBlocking = { viewModel.toggleShortsBlocking(it) },
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            NavigationGrid(
                onNavigateToBlockList = onNavigateToBlockList,
                onNavigateToStats = onNavigateToStats,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun HomeHeader(onNavigateToSettings: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Foccus",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        color = OnBackground,
                        fontWeight = FontWeight.Light,
                        letterSpacing = 1.sp
                    )
                )
                Text(
                    text = "FOCO MÁXIMO. RESULTADOS REAIS.",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = OnSurfaceVariant,
                        letterSpacing = 3.sp
                    )
                )
            }

            IconButton(
                onClick = onNavigateToSettings,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(SurfaceVariantDark)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = "Configurações",
                    tint = OnSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun QuickStatsSection(
    focusMinutes: Int,
    sessionsToday: Int,
    blockedApps: Int,
    modifier: Modifier = Modifier
) {
    val hours = focusMinutes / 60
    val minutes = focusMinutes % 60
    val timeFormatted = if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        MiniStatCard(
            label = "FOCO HOJE",
            value = timeFormatted,
            modifier = Modifier.weight(1f)
        )
        MiniStatCard(
            label = "SESSÕES",
            value = sessionsToday.toString(),
            modifier = Modifier.weight(1f)
        )
        MiniStatCard(
            label = "BLOQUEADOS",
            value = blockedApps.toString(),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun MiniStatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(SurfaceDark)
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Normal,
                    color = OnBackground
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = OnSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            )
        }
    }
}

@Composable
private fun FocusStartSection(
    hasActiveSession: Boolean,
    onStartFocus: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(SurfaceDark)
            .clickable { onStartFocus() }
            .padding(24.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (hasActiveSession) "SESSÃO ATIVA" else "INICIAR SESSÃO",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = OnBackground,
                        fontWeight = FontWeight.Light
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (hasActiveSession) "Toque para ver o progresso"
                    else "Comece agora e entre em modo foco",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = OnSurfaceVariant
                    )
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Accent),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = null,
                    tint = BackgroundDark,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
private fun QuickTogglesSection(
    isBlockingEnabled: Boolean,
    isGrayscaleEnabled: Boolean,
    isShortsBlockingEnabled: Boolean,
    onToggleBlocking: (Boolean) -> Unit,
    onToggleGrayscale: (Boolean) -> Unit,
    onToggleShortsBlocking: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "CONTROLES",
            style = MaterialTheme.typography.labelSmall.copy(
                color = OnSurfaceVariant,
                letterSpacing = 3.sp
            )
        )
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ToggleCard(
                label = "BLOQUEIO",
                description = "Apps distração",
                icon = Icons.Filled.Block,
                isEnabled = isBlockingEnabled,
                onToggle = onToggleBlocking,
                modifier = Modifier.weight(1f)
            )
            ToggleCard(
                label = "ESCALA CINZA",
                description = "Monocromático",
                icon = Icons.Outlined.Tonality,
                isEnabled = isGrayscaleEnabled,
                onToggle = onToggleGrayscale,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        ToggleCard(
            label = "SHORTS",
            description = "Bloquear YouTube Shorts",
            icon = Icons.Filled.VideoLibrary,
            isEnabled = isShortsBlockingEnabled,
            onToggle = onToggleShortsBlocking,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ToggleCard(
    label: String,
    description: String,
    icon: ImageVector,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(if (isEnabled) SurfaceElevatedDark else SurfaceDark)
            .clickable { onToggle(!isEnabled) }
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isEnabled) OnBackground else OnSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
                Switch(
                    checked = isEnabled,
                    onCheckedChange = onToggle,
                    modifier = Modifier
                        .height(24.dp)
                        .padding(0.dp),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = BackgroundDark,
                        checkedTrackColor = Accent,
                        uncheckedThumbColor = OnSurfaceVariant,
                        uncheckedTrackColor = SurfaceVariantDark
                    )
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall.copy(
                    color = if (isEnabled) OnBackground else OnSurface,
                    fontWeight = FontWeight.Light
                )
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = OnSurfaceVariant
                )
            )
        }
    }
}

@Composable
private fun NavigationGrid(
    onNavigateToBlockList: () -> Unit,
    onNavigateToStats: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "EXPLORAR",
            style = MaterialTheme.typography.labelSmall.copy(
                color = OnSurfaceVariant,
                letterSpacing = 3.sp
            )
        )
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            NavCard(
                label = "LISTA DE BLOQUEIO",
                description = "Gerencie apps bloqueados",
                icon = Icons.Filled.Block,
                onClick = onNavigateToBlockList,
                modifier = Modifier.weight(1f)
            )
            NavCard(
                label = "ESTATÍSTICAS",
                description = "Seu progresso de foco",
                icon = Icons.Filled.BarChart,
                onClick = onNavigateToStats,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun NavCard(
    label: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(SurfaceDark)
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(SurfaceVariantDark),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = OnSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall.copy(
                    color = OnBackground,
                    fontWeight = FontWeight.Light
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = OnSurfaceVariant
                )
            )
        }
    }
}
