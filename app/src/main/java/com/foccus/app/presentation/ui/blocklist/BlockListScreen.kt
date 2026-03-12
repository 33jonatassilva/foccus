package com.foccus.app.presentation.ui.blocklist

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.foccus.app.presentation.components.FoccusTopBar
import com.foccus.app.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockListScreen(
    onNavigateBack: () -> Unit,
    viewModel: BlockListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            FoccusTopBar(
                title = "Apps Bloqueados",
                onNavigateBack = onNavigateBack,
                actions = {
                    IconButton(onClick = { viewModel.toggleShowOnlyBlocked() }) {
                        Icon(
                            imageVector = Icons.Outlined.FilterList,
                            contentDescription = "Filtrar",
                            tint = if (uiState.showOnlyBlocked) Accent else OnSurfaceVariant
                        )
                    }
                }
            )
        },
        containerColor = BackgroundDark
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            SearchBar(
                query = uiState.searchQuery,
                onQueryChange = { viewModel.updateSearchQuery(it) },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            if (uiState.showOnlyBlocked) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(SurfaceVariantDark)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.FilterList,
                        contentDescription = null,
                        tint = OnSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "MOSTRANDO APENAS APPS BLOQUEADOS",
                        style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceVariant)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Accent)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = uiState.filteredApps,
                        key = { it.packageName }
                    ) { app ->
                        AppBlockItem(
                            item = app,
                            onToggleBlock = { viewModel.toggleBlockApp(app) },
                            onToggleEnabled = { enabled ->
                                viewModel.toggleEnabled(app.packageName, enabled)
                            }
                        )
                    }

                    if (uiState.filteredApps.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 48.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Filled.SearchOff,
                                        contentDescription = null,
                                        tint = OnSurfaceVariant,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Nenhum app encontrado",
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            color = OnSurfaceVariant
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = {
            Text(
                "Pesquisar apps...",
                style = MaterialTheme.typography.bodyMedium.copy(color = OnSurfaceVariant)
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = null,
                tint = OnSurfaceVariant
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Filled.Clear,
                        contentDescription = "Limpar",
                        tint = OnSurfaceVariant
                    )
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(4.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = SurfaceDark,
            unfocusedContainerColor = SurfaceDark,
            focusedBorderColor = Accent,
            unfocusedBorderColor = OutlineVariant,
            focusedTextColor = OnBackground,
            unfocusedTextColor = OnBackground,
            cursorColor = Accent
        )
    )
}

@Composable
private fun AppBlockItem(
    item: AppListItem,
    onToggleBlock: () -> Unit,
    onToggleEnabled: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(SurfaceDark)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(SurfaceVariantDark),
            contentAlignment = Alignment.Center
        ) {
            if (item.icon != null) {
                val bitmap = remember(item.packageName) {
                    item.icon.toBitmap(46, 46).asImageBitmap()
                }
                androidx.compose.foundation.Image(
                    painter = BitmapPainter(bitmap),
                    contentDescription = item.appName,
                    modifier = Modifier.size(40.dp).clip(RoundedCornerShape(4.dp))
                )
            } else {
                Icon(
                    imageVector = Icons.Filled.Apps,
                    contentDescription = null,
                    tint = OnSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.appName,
                style = MaterialTheme.typography.titleSmall.copy(
                    color = OnBackground,
                    fontWeight = FontWeight.Light
                )
            )
            if (item.isBlocked && item.blockedCount > 0) {
                Text(
                    text = "${item.blockedCount}x bloqueado",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = OnSurfaceVariant
                    )
                )
            }
        }

        AnimatedVisibility(visible = item.isBlocked) {
            Switch(
                checked = item.isEnabled,
                onCheckedChange = onToggleEnabled,
                modifier = Modifier.padding(end = 8.dp),
                colors = SwitchDefaults.colors(
                    checkedThumbColor = BackgroundDark,
                    checkedTrackColor = Accent,
                    uncheckedTrackColor = SurfaceVariantDark
                )
            )
        }

        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(
                    if (item.isBlocked) Accent.copy(alpha = 0.12f)
                    else SurfaceVariantDark
                ),
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                onClick = onToggleBlock,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = if (item.isBlocked) Icons.Filled.LockOpen else Icons.Filled.Block,
                    contentDescription = if (item.isBlocked) "Desbloquear" else "Bloquear",
                    tint = if (item.isBlocked) Accent else OnSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
