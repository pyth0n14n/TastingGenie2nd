package io.github.pyth0n14n.tastinggenie.feature.sake.edit

import io.github.pyth0n14n.tastinggenie.domain.model.enums.SakeClassification
import io.github.pyth0n14n.tastinggenie.domain.model.enums.SakeGrade

internal const val SAKE_ROW_REQUIRED_HINT = "required_hint"
internal const val SAKE_ROW_NAME = "name"
internal const val SAKE_ROW_GRADE = "grade"
internal const val SAKE_ROW_IMAGE = "image"
internal const val SAKE_ROW_GRADE_OTHER = "grade_other"
internal const val SAKE_ROW_CLASSIFICATION = "classification"
internal const val SAKE_ROW_CLASSIFICATION_OTHER = "classification_other"
internal const val SAKE_ROW_MAKER = "maker"
internal const val SAKE_ROW_PREFECTURE = "prefecture"
internal const val SAKE_ROW_SAKE_DEGREE = "sake_degree"
internal const val SAKE_ROW_ACIDITY = "acidity"
internal const val SAKE_ROW_ALCOHOL = "alcohol"
internal const val SAKE_ROW_KOJI_MAI = "koji_mai"
internal const val SAKE_ROW_KOJI_POLISH = "koji_polish"
internal const val SAKE_ROW_KAKE_MAI = "kake_mai"
internal const val SAKE_ROW_KAKE_POLISH = "kake_polish"
internal const val SAKE_ROW_YEAST = "yeast"
internal const val SAKE_ROW_WATER = "water"
internal const val SAKE_ROW_ERROR = "error"
internal const val SAKE_ROW_SAVE = "save"

internal fun SakeEditUiState.firstInvalidFieldIndex(): Int? {
    if (validationErrors.isEmpty()) {
        return null
    }
    val invalidRowKeys = validationErrors.keys.mapTo(mutableSetOf(), ::validationFieldRowKey)
    return visibleSakeEditRowKeys().indexOfFirst { rowKey -> rowKey in invalidRowKeys }.takeIf { it >= 0 }
}

internal fun SakeEditUiState.visibleSakeEditRowKeys(): List<String> =
    buildList {
        add(SAKE_ROW_REQUIRED_HINT)
        add(SAKE_ROW_NAME)
        add(SAKE_ROW_GRADE)
        add(SAKE_ROW_IMAGE)
        if (grade == SakeGrade.OTHER) {
            add(SAKE_ROW_GRADE_OTHER)
        }
        add(SAKE_ROW_CLASSIFICATION)
        if (classifications.contains(SakeClassification.OTHER)) {
            add(SAKE_ROW_CLASSIFICATION_OTHER)
        }
        add(SAKE_ROW_MAKER)
        add(SAKE_ROW_PREFECTURE)
        add(SAKE_ROW_SAKE_DEGREE)
        add(SAKE_ROW_ACIDITY)
        add(SAKE_ROW_ALCOHOL)
        add(SAKE_ROW_KOJI_MAI)
        add(SAKE_ROW_KOJI_POLISH)
        add(SAKE_ROW_KAKE_MAI)
        add(SAKE_ROW_KAKE_POLISH)
        add(SAKE_ROW_YEAST)
        add(SAKE_ROW_WATER)
        add(SAKE_ROW_ERROR)
        add(SAKE_ROW_SAVE)
    }

private fun validationFieldRowKey(field: SakeValidationField): String =
    when (field) {
        SakeValidationField.NAME -> SAKE_ROW_NAME
        SakeValidationField.GRADE -> SAKE_ROW_GRADE
        SakeValidationField.SAKE_DEGREE -> SAKE_ROW_SAKE_DEGREE
        SakeValidationField.ACIDITY -> SAKE_ROW_ACIDITY
        SakeValidationField.KOJI_POLISH -> SAKE_ROW_KOJI_POLISH
        SakeValidationField.KAKE_POLISH -> SAKE_ROW_KAKE_POLISH
        SakeValidationField.ALCOHOL -> SAKE_ROW_ALCOHOL
    }
