package io.github.pyth0n14n.tastinggenie.navigation

object AppDestination {
    const val ARG_SAKE_ID = "sakeId"
    const val ARG_REVIEW_ID = "reviewId"
    const val ARG_FOOD_REVIEW_ID = "foodReviewId"
    const val ARG_REVIEW_SECTION = "section"
    const val RESULT_REVIEW_REFRESH = "reviewRefresh"
    const val RESULT_REVIEW_LIST_TAB = "reviewListTab"
    const val REVIEW_LIST_TAB_SAKE = "SAKE_REVIEW"
    const val REVIEW_LIST_TAB_FOOD = "FOOD_REVIEW"
    const val NO_ID = -1L

    const val SAKE_LIST = "sake/list"
    const val SAKE_EDIT = "sake/edit?$ARG_SAKE_ID={$ARG_SAKE_ID}"
    const val REVIEW_LIST = "review/list/{$ARG_SAKE_ID}"
    const val REVIEW_EDIT_CREATE = "review/edit/{$ARG_SAKE_ID}?$ARG_REVIEW_SECTION={$ARG_REVIEW_SECTION}"
    const val REVIEW_EDIT = "review/edit/{$ARG_SAKE_ID}/{$ARG_REVIEW_ID}?$ARG_REVIEW_SECTION={$ARG_REVIEW_SECTION}"
    const val FOOD_REVIEW_EDIT_CREATE = "review/food/edit/{$ARG_SAKE_ID}"
    const val FOOD_REVIEW_EDIT = "review/food/edit/{$ARG_SAKE_ID}/{$ARG_FOOD_REVIEW_ID}"
    const val REVIEW_DETAIL = "review/detail/{$ARG_REVIEW_ID}"
    const val REVIEW_IMAGE = "review/image/{$ARG_REVIEW_ID}"
    const val SAKE_IMAGE = "sake/image/{$ARG_SAKE_ID}"
    const val HELP = "help"
    const val SETTINGS = "settings"

    fun sakeEditRoute(sakeId: Long?): String {
        val targetId = sakeId ?: return "sake/edit"
        return "sake/edit?$ARG_SAKE_ID=$targetId"
    }

    fun reviewListRoute(sakeId: Long): String = "review/list/$sakeId"

    fun reviewEditRoute(
        sakeId: Long,
        reviewId: Long?,
        sectionName: String = "BASIC",
    ): String =
        if (reviewId == null) {
            "review/edit/$sakeId?$ARG_REVIEW_SECTION=$sectionName"
        } else {
            "review/edit/$sakeId/$reviewId?$ARG_REVIEW_SECTION=$sectionName"
        }

    fun reviewDetailRoute(reviewId: Long): String = "review/detail/$reviewId"

    fun foodReviewEditRoute(
        sakeId: Long,
        foodReviewId: Long?,
    ): String =
        if (foodReviewId == null) {
            "review/food/edit/$sakeId"
        } else {
            "review/food/edit/$sakeId/$foodReviewId"
        }

    fun reviewImageRoute(reviewId: Long): String = "review/image/$reviewId"

    fun sakeImageRoute(sakeId: Long): String = "sake/image/$sakeId"
}
