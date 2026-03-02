package io.github.pyth0n14n.tastinggenie.navigation

object AppDestination {
    const val ARG_SAKE_ID = "sakeId"
    const val NO_ID = -1L

    const val SAKE_LIST = "sake/list"
    const val SAKE_EDIT = "sake/edit?$ARG_SAKE_ID={$ARG_SAKE_ID}"

    fun sakeEditRoute(sakeId: Long?): String {
        val targetId = sakeId ?: return "sake/edit"
        return "sake/edit?$ARG_SAKE_ID=$targetId"
    }
}
