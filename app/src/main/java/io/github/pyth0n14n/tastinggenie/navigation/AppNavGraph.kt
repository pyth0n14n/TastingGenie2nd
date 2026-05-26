package io.github.pyth0n14n.tastinggenie.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import io.github.pyth0n14n.tastinggenie.feature.help.HelpRoute
import io.github.pyth0n14n.tastinggenie.feature.onboarding.OnboardingRoute
import io.github.pyth0n14n.tastinggenie.feature.review.ReviewSection
import io.github.pyth0n14n.tastinggenie.feature.review.detail.ReviewDetailRoute
import io.github.pyth0n14n.tastinggenie.feature.review.edit.ReviewEditRoute
import io.github.pyth0n14n.tastinggenie.feature.review.food.SakeFoodReviewEditRoute
import io.github.pyth0n14n.tastinggenie.feature.review.image.ReviewImageRoute
import io.github.pyth0n14n.tastinggenie.feature.review.list.ReviewListRoute
import io.github.pyth0n14n.tastinggenie.feature.sake.edit.SakeEditRoute
import io.github.pyth0n14n.tastinggenie.feature.sake.list.SakeListRoute
import io.github.pyth0n14n.tastinggenie.feature.sake.list.SakeListRouteActions
import io.github.pyth0n14n.tastinggenie.feature.sake.list.SakeListTopBarActions
import io.github.pyth0n14n.tastinggenie.feature.settings.SettingsRoute
import io.github.pyth0n14n.tastinggenie.ui.common.LoadingContent

@Composable
fun AppNavGraph(startViewModel: AppStartViewModel = hiltViewModel()) {
    val settings by startViewModel.settings.collectAsStateWithLifecycle()
    val currentSettings =
        settings ?: run {
            LoadingContent()
            return
        }
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination =
            if (currentSettings.onboardingCompleted) {
                AppDestination.SAKE_LIST
            } else {
                AppDestination.ONBOARDING
            },
    ) {
        addOnboardingGraph(navController)
        addSakeGraph(navController)
        addReviewGraph(navController)
    }
}

private fun NavGraphBuilder.addOnboardingGraph(navController: NavHostController) {
    composable(AppDestination.ONBOARDING) {
        OnboardingRoute(
            onSkip = {
                navController.navigate(AppDestination.SAKE_LIST) {
                    popUpTo(AppDestination.ONBOARDING) { inclusive = true }
                }
            },
            onCreateSake = {
                navController.navigate(AppDestination.sakeEditRoute(sakeId = null)) {
                    popUpTo(AppDestination.ONBOARDING) { inclusive = true }
                }
            },
        )
    }
}

private fun NavGraphBuilder.addSakeGraph(navController: NavHostController) {
    composable(AppDestination.SAKE_LIST) {
        SakeListRoute(
            actions =
                SakeListRouteActions(
                    onCreateSake = { navController.navigate(AppDestination.sakeEditRoute(sakeId = null)) },
                    onOpenSake = { sakeId -> navController.navigate(AppDestination.reviewListRoute(sakeId = sakeId)) },
                    onEditSake = { sakeId -> navController.navigate(AppDestination.sakeEditRoute(sakeId = sakeId)) },
                    onOpenSakeImage = { sakeId ->
                        navController.navigate(AppDestination.sakeImageRoute(sakeId = sakeId))
                    },
                    topBarActions =
                        SakeListTopBarActions(
                            onOpenSettings = { navController.navigate(AppDestination.SETTINGS) },
                        ),
                ),
        )
    }
    composable(
        route = AppDestination.SAKE_EDIT,
        arguments =
            listOf(
                navArgument(AppDestination.ARG_SAKE_ID) {
                    type = NavType.LongType
                    defaultValue = AppDestination.NO_ID
                },
            ),
    ) {
        SakeEditRoute(onBack = { navController.popBackStackIfPossible() })
    }
    composable(AppDestination.HELP) {
        HelpRoute(onBack = { navController.popBackStackIfPossible() })
    }
    composable(AppDestination.SETTINGS) {
        val sakeListEntry = remember(it) { navController.getBackStackEntry(AppDestination.SAKE_LIST) }
        SettingsRoute(
            onBack = { navController.popBackStackIfPossible() },
            onOpenGlossary = { navController.navigate(AppDestination.HELP) },
            viewModel = hiltViewModel(sakeListEntry),
        )
    }
}

private fun NavGraphBuilder.addReviewGraph(navController: NavHostController) {
    composable(
        route = AppDestination.REVIEW_LIST,
        arguments =
            listOf(
                navArgument(AppDestination.ARG_SAKE_ID) {
                    type = NavType.LongType
                },
            ),
    ) { backStackEntry ->
        val requestedTabName by
            backStackEntry.savedStateHandle
                .getStateFlow(AppDestination.RESULT_REVIEW_LIST_TAB, AppDestination.REVIEW_LIST_TAB_SAKE)
                .collectAsStateWithLifecycle()
        ReviewListRoute(
            initialTabName = requestedTabName,
            onBack = { navController.popBackStackIfPossible() },
            onAddReview = { sakeId ->
                navController.navigate(AppDestination.reviewEditRoute(sakeId = sakeId, reviewId = null))
            },
            onAddFoodReview = { sakeId ->
                navController.navigate(AppDestination.foodReviewEditRoute(sakeId = sakeId, foodReviewId = null))
            },
            onOpenReview = { reviewId ->
                navController.navigate(AppDestination.reviewDetailRoute(reviewId = reviewId))
            },
            onOpenFoodReview = { sakeId, foodReviewId ->
                navController.navigate(
                    AppDestination.foodReviewEditRoute(sakeId = sakeId, foodReviewId = foodReviewId),
                )
            },
            onOpenSakeImage = { sakeId ->
                navController.navigate(AppDestination.sakeImageRoute(sakeId = sakeId))
            },
        )
    }
    addReviewEditDestinations(navController)
    addFoodReviewEditDestinations(navController)
    addReviewDetailDestination(navController)
    addReviewImageDestination(navController)
}

private fun NavGraphBuilder.addReviewEditDestinations(navController: NavHostController) {
    composable(
        route = AppDestination.REVIEW_EDIT_CREATE,
        arguments =
            listOf(
                navArgument(AppDestination.ARG_SAKE_ID) {
                    type = NavType.LongType
                },
                navArgument(AppDestination.ARG_REVIEW_SECTION) {
                    type = NavType.StringType
                    defaultValue = "BASIC"
                },
            ),
    ) { backStackEntry ->
        val initialSection = backStackEntry.reviewSectionArgument()
        ReviewEditRoute(
            onBack = { navController.popBackStackIfPossible() },
            onSaved = {
                navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.set(AppDestination.RESULT_REVIEW_REFRESH, true)
                navController.popBackStackIfPossible()
            },
            initialSection = initialSection,
        )
    }
    composable(
        route = AppDestination.REVIEW_EDIT,
        arguments =
            listOf(
                navArgument(AppDestination.ARG_SAKE_ID) {
                    type = NavType.LongType
                },
                navArgument(AppDestination.ARG_REVIEW_ID) {
                    type = NavType.LongType
                },
                navArgument(AppDestination.ARG_REVIEW_SECTION) {
                    type = NavType.StringType
                    defaultValue = "BASIC"
                },
            ),
    ) { backStackEntry ->
        val initialSection = backStackEntry.reviewSectionArgument()
        ReviewEditRoute(
            onBack = { navController.popBackStackIfPossible() },
            onSaved = {
                navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.set(AppDestination.RESULT_REVIEW_REFRESH, true)
                navController.popBackStackIfPossible()
            },
            initialSection = initialSection,
        )
    }
}

private fun NavGraphBuilder.addFoodReviewEditDestinations(navController: NavHostController) {
    composable(
        route = AppDestination.FOOD_REVIEW_EDIT_CREATE,
        arguments =
            listOf(
                navArgument(AppDestination.ARG_SAKE_ID) {
                    type = NavType.LongType
                },
            ),
    ) {
        SakeFoodReviewEditRoute(
            onBack = {
                navController.selectFoodReviewListTab()
                navController.popBackStackIfPossible()
            },
            onSaved = {
                navController.selectFoodReviewListTab()
                navController.popBackStackIfPossible()
            },
        )
    }
    composable(
        route = AppDestination.FOOD_REVIEW_EDIT,
        arguments =
            listOf(
                navArgument(AppDestination.ARG_SAKE_ID) {
                    type = NavType.LongType
                },
                navArgument(AppDestination.ARG_FOOD_REVIEW_ID) {
                    type = NavType.LongType
                },
            ),
    ) {
        SakeFoodReviewEditRoute(
            onBack = {
                navController.selectFoodReviewListTab()
                navController.popBackStackIfPossible()
            },
            onSaved = {
                navController.selectFoodReviewListTab()
                navController.popBackStackIfPossible()
            },
        )
    }
}

private fun NavHostController.selectFoodReviewListTab() {
    previousBackStackEntry
        ?.savedStateHandle
        ?.set(AppDestination.RESULT_REVIEW_LIST_TAB, AppDestination.REVIEW_LIST_TAB_FOOD)
}

private fun NavGraphBuilder.addReviewDetailDestination(navController: NavHostController) {
    composable(
        route = AppDestination.REVIEW_DETAIL,
        arguments =
            listOf(
                navArgument(AppDestination.ARG_REVIEW_ID) {
                    type = NavType.LongType
                },
            ),
    ) { backStackEntry ->
        val refreshRequested by
            backStackEntry.savedStateHandle
                .getStateFlow(AppDestination.RESULT_REVIEW_REFRESH, false)
                .collectAsStateWithLifecycle()
        ReviewDetailRoute(
            onBack = { navController.popBackStackIfPossible() },
            onEditReview = { sakeId, reviewId, section ->
                navController.navigate(
                    AppDestination.reviewEditRoute(
                        sakeId = sakeId,
                        reviewId = reviewId,
                        sectionName = section.name,
                    ),
                )
            },
            refreshRequested = refreshRequested,
            onRefreshConsumed = {
                backStackEntry.savedStateHandle[AppDestination.RESULT_REVIEW_REFRESH] = false
            },
        )
    }
}

private fun NavBackStackEntry.reviewSectionArgument(): ReviewSection =
    arguments
        ?.getString(AppDestination.ARG_REVIEW_SECTION)
        ?.let { sectionName -> ReviewSection.entries.firstOrNull { section -> section.name == sectionName } }
        ?: ReviewSection.BASIC

private fun NavGraphBuilder.addReviewImageDestination(navController: NavHostController) {
    composable(
        route = AppDestination.REVIEW_IMAGE,
        arguments =
            listOf(
                navArgument(AppDestination.ARG_REVIEW_ID) {
                    type = NavType.LongType
                },
            ),
    ) {
        ReviewImageRoute(onBack = { navController.popBackStackIfPossible() })
    }
    composable(
        route = AppDestination.SAKE_IMAGE,
        arguments =
            listOf(
                navArgument(AppDestination.ARG_SAKE_ID) {
                    type = NavType.LongType
                },
            ),
    ) {
        ReviewImageRoute(onBack = { navController.popBackStackIfPossible() })
    }
}
