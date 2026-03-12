package com.foccus.app.presentation.navigation

import androidx.lifecycle.ViewModel
import com.foccus.app.data.local.preferences.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NavViewModel @Inject constructor(
    val preferences: UserPreferences
) : ViewModel()
