package io.github.pyth0n14n.tastinggenie.feature.onboarding

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import io.github.pyth0n14n.tastinggenie.R

@Composable
fun OnboardingRoute(
    onSkip: () -> Unit,
    onCreateSake: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    OnboardingScreen(
        pages = onboardingPages(),
        onSkip = { viewModel.complete(onSkip) },
        onCreateSake = { viewModel.complete(onCreateSake) },
    )
}

private fun onboardingPages(): List<OnboardingPage> =
    listOf(
        OnboardingPage(
            titleResId = R.string.onboarding_page_one_title,
            messageResId = R.string.onboarding_page_one_message,
            imageResId = R.drawable.walkthrough_register_sake,
        ),
        OnboardingPage(
            titleResId = R.string.onboarding_page_two_title,
            messageResId = R.string.onboarding_page_two_message,
            imageResId = R.drawable.walkthrough_review_sake,
        ),
        OnboardingPage(
            titleResId = R.string.onboarding_page_three_title,
            messageResId = R.string.onboarding_page_three_message,
            imageResId = R.drawable.walkthrough_select,
        ),
    )
