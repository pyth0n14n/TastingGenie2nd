package io.github.pyth0n14n.tastinggenie.navigation

import androidx.navigation.NavHostController

internal fun NavHostController.popBackStackIfPossible() {
    if (previousBackStackEntry != null) {
        popBackStack()
    }
}
