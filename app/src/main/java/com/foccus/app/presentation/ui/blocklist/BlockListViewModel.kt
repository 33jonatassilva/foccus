package com.foccus.app.presentation.ui.blocklist

import android.graphics.drawable.Drawable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foccus.app.domain.model.AppInfo
import com.foccus.app.domain.model.BlockedApp
import com.foccus.app.domain.repository.BlockedAppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AppListItem(
    val packageName: String,
    val appName: String,
    val icon: Drawable? = null,
    val isBlocked: Boolean = false,
    val isEnabled: Boolean = true,
    val blockedCount: Int = 0
)

data class BlockListUiState(
    val apps: List<AppListItem> = emptyList(),
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val showOnlyBlocked: Boolean = false
) {
    val filteredApps: List<AppListItem>
        get() {
            var list = apps
            if (showOnlyBlocked) list = list.filter { it.isBlocked }
            if (searchQuery.isNotBlank()) {
                list = list.filter { it.appName.contains(searchQuery, ignoreCase = true) }
            }
            return list
        }
}

@HiltViewModel
class BlockListViewModel @Inject constructor(
    private val repo: BlockedAppRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BlockListUiState())
    val uiState: StateFlow<BlockListUiState> = _uiState.asStateFlow()

    init {
        loadApps()
    }

    private fun loadApps() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val installed = repo.getInstalledApps()

            repo.getAllBlockedApps().collect { blockedApps ->
                val blockedMap = blockedApps.associateBy { it.packageName }

                val items = installed.map { appInfo ->
                    val blocked = blockedMap[appInfo.packageName]
                    AppListItem(
                        packageName = appInfo.packageName,
                        appName = appInfo.appName,
                        icon = appInfo.icon,
                        isBlocked = blocked != null,
                        isEnabled = blocked?.isEnabled ?: false,
                        blockedCount = blocked?.blockedCount ?: 0
                    )
                }.sortedWith(
                    compareByDescending<AppListItem> { it.isBlocked }
                        .thenBy { it.appName }
                )

                _uiState.update {
                    it.copy(apps = items, isLoading = false)
                }
            }
        }
    }

    fun toggleBlockApp(item: AppListItem) {
        viewModelScope.launch {
            if (item.isBlocked) {
                repo.removeBlockedApp(item.packageName)
            } else {
                repo.addBlockedApp(
                    BlockedApp(
                        packageName = item.packageName,
                        appName = item.appName,
                        isEnabled = true
                    )
                )
            }
        }
    }

    fun toggleEnabled(packageName: String, enabled: Boolean) {
        viewModelScope.launch {
            repo.setEnabled(packageName, enabled)
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun toggleShowOnlyBlocked() {
        _uiState.update { it.copy(showOnlyBlocked = !it.showOnlyBlocked) }
    }
}
