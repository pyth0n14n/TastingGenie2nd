package io.github.pyth0n14n.tastinggenie.feature.sake.edit

import io.github.pyth0n14n.tastinggenie.domain.model.MasterOption
import io.github.pyth0n14n.tastinggenie.domain.model.UiError
import io.github.pyth0n14n.tastinggenie.domain.model.enums.SakeGrade

data class SakeEditUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val error: UiError? = null,
    val sakeId: Long? = null,
    val name: String = "",
    val grade: SakeGrade? = null,
    val gradeOptions: List<MasterOption> = emptyList(),
)
