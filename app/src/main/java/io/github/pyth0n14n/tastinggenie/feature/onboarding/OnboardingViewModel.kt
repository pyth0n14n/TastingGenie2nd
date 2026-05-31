package io.github.pyth0n14n.tastinggenie.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.pyth0n14n.tastinggenie.domain.repository.SettingsRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel
    @Inject
    constructor(
        private val settingsRepository: SettingsRepository,
    ) : ViewModel() {
        fun complete(onCompleted: () -> Unit) {
            viewModelScope.launch {
                settingsRepository.updateOnboardingCompleted(completed = true)
                onCompleted()
            }
        }
    }
