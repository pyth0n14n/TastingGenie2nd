package io.github.pyth0n14n.tastinggenie.feature.onboarding

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class OnboardingPage(
    @param:StringRes val titleResId: Int,
    @param:StringRes val messageResId: Int,
    @param:DrawableRes val imageResId: Int? = null,
)
