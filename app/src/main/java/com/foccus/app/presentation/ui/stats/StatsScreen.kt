package com.foccus.app.presentation.ui.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.foccus.app.presentation.components.FoccusTopBar
import com.foccus.app.presentation.components.StatCard
import com.foccus.app.presentation.theme.*

@Composable
fun StatsScreen(
    onNavigateBack: () -> Unit,
    viewModel: StatsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { FoccusTopBar(title = "Estatísticas", onNavigateBack = onNavigateBack) },
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
            SectionTitle("HOJE")
            TodaySection(uiState = uiState)

            SectionTitle("ESTA SEMANA")
            WeekSection(uiState = uiState)

            SectionTitle("TOTAL")
            TotalSection(uiState = uiState)

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall.copy(
            color = OnSurfaceVariant,
            letterSpacing = 3.sp
        )
    )
}

@Composable
private fun TodaySection(uiState: StatsUiState) {
    val hours = uiState.todayMinutes / 60
    val minutes = uiState.todayMinutes % 60
    val timeText = if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(SurfaceDark)
            .padding(24.dp)
    ) {
        Column {
            Text(
                text = timeText,
                style = MaterialTheme.typography.displaySmall.copy(
                    color = OnBackground,
                    fontWeight = FontWeight.Light
                )
            )
            Text(
                text = "de foco hoje",
                style = MaterialTheme.typography.bodyMedium.copy(color = OnSurfaceVariant)
            )

            if (uiState.todayMinutes == 0) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Inicie sua primeira sessão hoje!",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = OnSurfaceVariant
                    )
                )
            }
        }
    }
}

@Composable
private fun WeekSection(uiState: StatsUiState) {
    val weekHours = uiState.weekMinutes / 60
    val weekMins = uiState.weekMinutes % 60
    val weekText = if (weekHours > 0) "${weekHours}h ${weekMins}m" else "${weekMins}m"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            title = "Tempo de Foco",
            value = weekText,
            subtitle = "nesta semana",
            icon = Icons.Filled.AccessTime,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            title = "Sessões",
            value = uiState.weekCompleted.toString(),
            subtitle = "concluídas",
            icon = Icons.Filled.CheckCircle,
            modifier = Modifier.weight(1f)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(SurfaceDark)
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(SurfaceVariantDark),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Block,
                        contentDescription = null,
                        tint = OnSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Tentativas de distração",
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = OnBackground,
                            fontWeight = FontWeight.Light
                        )
                    )
                    Text(
                        text = "apps bloqueados esta semana",
                        style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceVariant)
                    )
                }
            }
            Text(
                text = uiState.totalBlockedAttempts.toString(),
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = OnBackground,
                    fontWeight = FontWeight.Normal
                )
            )
        }
    }
}

@Composable
private fun TotalSection(uiState: StatsUiState) {
    val monthHours = uiState.monthMinutes / 60
    val monthMins = uiState.monthMinutes % 60
    val monthText = if (monthHours > 0) "${monthHours}h ${monthMins}m" else "${monthMins}m"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            title = "Sessões Completas",
            value = uiState.totalCompleted.toString(),
            subtitle = "de todos os tempos",
            icon = Icons.Filled.EmojiEvents,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            title = "Foco no Mês",
            value = monthText,
            subtitle = "este mês",
            icon = Icons.Filled.CalendarMonth,
            modifier = Modifier.weight(1f)
        )
    }
}
