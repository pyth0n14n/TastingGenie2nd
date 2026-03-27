package io.github.pyth0n14n.tastinggenie.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import io.github.pyth0n14n.tastinggenie.feature.help.HelpRoute
import io.github.pyth0n14n.tastinggenie.feature.review.detail.ReviewDetailRoute
import io.github.pyth0n14n.tastinggenie.feature.review.edit.ReviewEditRoute
import io.github.pyth0n14n.tastinggenie.feature.review.image.ReviewImageRoute
import io.github.pyth0n14n.tastinggenie.feature.review.list.ReviewListRoute
import io.github.pyth0n14n.tastinggenie.feature.sake.edit.SakeEditRoute
import io.github.pyth0n14n.tastinggenie.feature.sake.list.SakeListRoute
import io.github.pyth0n14n.tastinggenie.feature.sake.list.SakeListTopBarActions
import io.github.pyth0n14n.tastinggenie.feature.settings.SettingsRoute

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = AppDestination.SAKE_LIST,
    ) {
        addSakeGraph(navController)
        addReviewGraph(navController)
    }
}

private fun NavGraphBuilder.addSakeGraph(navController: NavHostController) {
    composable(AppDestination.SAKE_LIST) {
        SakeListRoute(
            onCreateSake = { navController.navigate(AppDestination.sakeEditRoute(sakeId = null)) },
            onOpenSake = { sakeId -> navController.navigate(AppDestination.reviewListRoute(sakeId = sakeId)) },
            onEditSake = { sakeId -> navController.navigate(AppDestination.sakeEditRoute(sakeId = sakeId)) },
            topBarActions =
                SakeListTopBarActions(
                    onOpenHelp = { navController.navigate(AppDestination.HELP) },
                    onOpenSettings = { navController.navigate(AppDestination.SETTINGS) },
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
        SakeEditRoute(onBack = { navController.popBackStack() })
    }
    composable(AppDestination.HELP) {
        HelpRoute(onBack = { navController.popBackStack() })
    }
    composable(AppDestination.SETTINGS) {
        val sakeListEntry = remember(it) { navController.getBackStackEntry(AppDestination.SAKE_LIST) }
        SettingsRoute(
            onBack = { navController.popBackStack() },
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
    ) {
        ReviewListRoute(
            onBack = { navController.popBackStack() },
            onAddReview = { sakeId ->
                navController.navigate(AppDestination.reviewEditRoute(sakeId = sakeId, reviewId = null))
            },
            onOpenReview = { reviewId ->
                navController.navigate(AppDestination.reviewDetailRoute(reviewId = reviewId))
            },
            onOpenImage = { reviewId ->
                navController.navigate(AppDestination.reviewImageRoute(reviewId = reviewId))
            },
        )
    }
    addReviewEditDestinations(navController)
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
            ),
    ) {
        ReviewEditRoute(
            onBack = { navController.popBackStack() },
            onSaved = {
                navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.set(AppDestination.RESULT_REVIEW_REFRESH, true)
                navController.popBackStack()
            },
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
            ),
    ) {
        ReviewEditRoute(
            onBack = { navController.popBackStack() },
            onSaved = {
                navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.set(AppDestination.RESULT_REVIEW_REFRESH, true)
                navController.popBackStack()
            },
        )
    }
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
            onBack = { navController.popBackStack() },
            onEditReview = { sakeId, reviewId ->
                navController.navigate(AppDestination.reviewEditRoute(sakeId = sakeId, reviewId = reviewId))
            },
            refreshRequested = refreshRequested,
            onRefreshConsumed = {
                backStackEntry.savedStateHandle[AppDestination.RESULT_REVIEW_REFRESH] = false
            },
        )
    }
}

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
        ReviewImageRoute(onBack = { navController.popBackStack() })
    }
}
