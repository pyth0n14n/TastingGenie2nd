package io.github.pyth0n14n.tastinggenie.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.pyth0n14n.tastinggenie.domain.model.AppSettings
import io.github.pyth0n14n.tastinggenie.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

private const val SETTINGS_SUBSCRIPTION_TIMEOUT_MILLIS = 5_000L

@HiltViewModel
class AppStartViewModel
    @Inject
    constructor(
        settingsRepository: SettingsRepository,
    ) : ViewModel() {
        val settings: StateFlow<AppSettings?> =
            settingsRepository
                .observeSettings()
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(SETTINGS_SUBSCRIPTION_TIMEOUT_MILLIS),
                    initialValue = null,
                )
    }
