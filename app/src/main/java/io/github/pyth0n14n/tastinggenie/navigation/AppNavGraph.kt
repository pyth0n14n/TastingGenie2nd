package io.github.pyth0n14n.tastinggenie.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import io.github.pyth0n14n.tastinggenie.feature.sake.edit.SakeEditRoute
import io.github.pyth0n14n.tastinggenie.feature.sake.list.SakeListRoute

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = AppDestination.SAKE_LIST,
    ) {
        composable(AppDestination.SAKE_LIST) {
            SakeListRoute(
                onCreateSake = { navController.navigate(AppDestination.sakeEditRoute(sakeId = null)) },
                onOpenSake = { sakeId -> navController.navigate(AppDestination.sakeEditRoute(sakeId = sakeId)) },
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
    }
}
