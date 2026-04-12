package io.github.pyth0n14n.tastinggenie.feature.sake.edit

import io.github.pyth0n14n.tastinggenie.domain.model.MasterOption
import io.github.pyth0n14n.tastinggenie.domain.model.UiError
import io.github.pyth0n14n.tastinggenie.domain.model.enums.Prefecture
import io.github.pyth0n14n.tastinggenie.domain.model.enums.SakeClassification
import io.github.pyth0n14n.tastinggenie.domain.model.enums.SakeGrade
import io.github.pyth0n14n.tastinggenie.ui.common.FieldValidationError

data class SakeEditUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val isEditTargetMissing: Boolean = false,
    val error: UiError? = null,
    val validationErrors: Map<SakeValidationField, FieldValidationError> = emptyMap(),
    val validationFailureCount: Int = 0,
    val sakeId: Long? = null,
    val isPinned: Boolean = false,
    val name: String = "",
    val grade: SakeGrade? = null,
    val gradeOptions: List<MasterOption> = emptyList(),
    val imagePreviewUri: String? = null,
    val persistedImageUri: String? = null,
    val pendingImageSourceUri: String? = null,
    val isImageMarkedForDeletion: Boolean = false,
    val gradeOther: String = "",
    val classifications: List<SakeClassification> = emptyList(),
    val classificationOptions: List<MasterOption> = emptyList(),
    val typeOther: String = "",
    val maker: String = "",
    val prefecture: Prefecture? = null,
    val prefectureOptions: List<MasterOption> = emptyList(),
    val sakeDegree: String = "",
    val acidity: String = "",
    val kojiMai: String = "",
    val kojiPolish: String = "",
    val kakeMai: String = "",
    val kakePolish: String = "",
    val alcohol: String = "",
    val yeast: String = "",
    val water: String = "",
)
