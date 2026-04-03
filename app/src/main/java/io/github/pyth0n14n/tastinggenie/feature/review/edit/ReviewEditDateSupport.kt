package io.github.pyth0n14n.tastinggenie.feature.review.edit

import java.time.Instant
import java.time.ZoneOffset

fun ReviewEditUiState.withDateSelected(epochMillis: Long): ReviewEditUiState =
    copy(
        date = epochMillis.toReviewDateText(),
        error = null,
    )

fun String.toDatePickerInitialMillisOrNull(): Long? =
    toLocalDateOrNull()
        ?.atStartOfDay(ZoneOffset.UTC)
        ?.toInstant()
        ?.toEpochMilli()

fun Long.toReviewDateText(): String =
    Instant
        .ofEpochMilli(this)
        .atZone(ZoneOffset.UTC)
        .toLocalDate()
        .toString()
