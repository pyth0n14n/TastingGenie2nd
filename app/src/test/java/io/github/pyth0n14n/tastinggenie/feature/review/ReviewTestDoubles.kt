package io.github.pyth0n14n.tastinggenie.feature.review

import io.github.pyth0n14n.tastinggenie.domain.model.AromaCategoryMaster
import io.github.pyth0n14n.tastinggenie.domain.model.MasterDataBundle
import io.github.pyth0n14n.tastinggenie.domain.model.MasterOption
import io.github.pyth0n14n.tastinggenie.domain.model.Review
import io.github.pyth0n14n.tastinggenie.domain.model.ReviewId
import io.github.pyth0n14n.tastinggenie.domain.model.ReviewInput
import io.github.pyth0n14n.tastinggenie.domain.model.Sake
import io.github.pyth0n14n.tastinggenie.domain.model.SakeDeleteResult
import io.github.pyth0n14n.tastinggenie.domain.model.SakeId
import io.github.pyth0n14n.tastinggenie.domain.model.SakeInput
import io.github.pyth0n14n.tastinggenie.domain.model.SakeListSummary
import io.github.pyth0n14n.tastinggenie.domain.model.enums.Aroma
import io.github.pyth0n14n.tastinggenie.domain.model.enums.AromaGroup
import io.github.pyth0n14n.tastinggenie.domain.model.enums.IntensityLevel
import io.github.pyth0n14n.tastinggenie.domain.model.enums.OverallReview
import io.github.pyth0n14n.tastinggenie.domain.model.enums.SakeColor
import io.github.pyth0n14n.tastinggenie.domain.model.enums.SakeGrade
import io.github.pyth0n14n.tastinggenie.domain.model.enums.TasteLevel
import io.github.pyth0n14n.tastinggenie.domain.model.enums.Temperature
import io.github.pyth0n14n.tastinggenie.domain.repository.MasterDataRepository
import io.github.pyth0n14n.tastinggenie.domain.repository.ReviewRepository
import io.github.pyth0n14n.tastinggenie.domain.repository.SakeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

internal const val TEST_SAKE_ID = 7L
internal const val TEST_REVIEW_ID = 11L
private const val TEST_REVIEW_DATE = "2026-03-14"

internal class RecordingSakeRepository(
    initial: List<Sake> = emptyList(),
) : SakeRepository {
    private val stream = MutableStateFlow(initial)
    val savedInputs = mutableListOf<SakeInput>()

    override fun observeSakes(): Flow<List<Sake>> = stream

    override fun observeSakeListSummaries(): Flow<List<SakeListSummary>> =
        stream.map { list -> list.map { SakeListSummary(it) } }

    override suspend fun getSake(id: SakeId): Sake? = stream.value.firstOrNull { it.id == id }

    override suspend fun upsertSake(input: SakeInput): SakeId {
        savedInputs.add(input)
        val id = input.id ?: ((stream.value.maxOfOrNull { it.id } ?: 0L) + 1L)
        val mapped =
            Sake(
                id = id,
                name = input.name,
                grade = input.grade,
                isPinned = input.isPinned,
                imageUris = input.imageUris,
            )
        val mutable = stream.value.toMutableList().apply { removeAll { it.id == id } }
        mutable.add(mapped)
        stream.value = mutable
        return id
    }

    override suspend fun setPinned(
        id: SakeId,
        isPinned: Boolean,
    ) {
        stream.value =
            stream.value.map { sake ->
                if (sake.id == id) {
                    sake.copy(isPinned = isPinned)
                } else {
                    sake
                }
            }
    }

    override suspend fun deleteSake(id: SakeId): SakeDeleteResult {
        val removed = stream.value.any { sake -> sake.id == id }
        if (removed) {
            stream.value = stream.value.filterNot { sake -> sake.id == id }
        }
        return SakeDeleteResult(isDeleted = removed)
    }
}

internal class RecordingReviewRepository(
    initial: List<Review> = emptyList(),
) : ReviewRepository {
    private val stream = MutableStateFlow(initial)
    val savedInputs = mutableListOf<ReviewInput>()
    val deletedReviewIds = mutableListOf<ReviewId>()
    var deleteFailure: Throwable? = null

    override fun observeReviews(sakeId: SakeId): Flow<List<Review>> =
        stream.map { reviews -> reviews.filter { it.sakeId == sakeId } }

    override suspend fun getReview(id: ReviewId): Review? = stream.value.firstOrNull { it.id == id }

    override suspend fun upsertReview(input: ReviewInput): ReviewId {
        savedInputs.add(input)
        val id = input.id ?: ((stream.value.maxOfOrNull { it.id } ?: 0L) + 1L)
        val mapped =
            Review(
                id = id,
                sakeId = input.sakeId,
                date = input.date,
                bar = input.bar,
                price = input.price,
                volume = input.volume,
                temperature = input.temperature,
                scene = input.scene,
                dish = input.dish,
                appearanceSoundness = input.appearanceSoundness,
                appearanceColor = input.appearanceColor,
                appearanceViscosity = input.appearanceViscosity,
                aromaSoundness = input.aromaSoundness,
                aromaIntensity = input.aromaIntensity,
                aromaExamples = input.aromaExamples,
                aromaMainNote = input.aromaMainNote,
                aromaComplexity = input.aromaComplexity,
                tasteSoundness = input.tasteSoundness,
                tasteAttack = input.tasteAttack,
                tasteTextureRoundness = input.tasteTextureRoundness,
                tasteTextureSmoothness = input.tasteTextureSmoothness,
                tasteMainNote = input.tasteMainNote,
                tasteSweetness = input.tasteSweetness,
                tasteSourness = input.tasteSourness,
                tasteBitterness = input.tasteBitterness,
                tasteUmami = input.tasteUmami,
                tasteInPalateAroma = input.tasteInPalateAroma,
                tasteAftertaste = input.tasteAftertaste,
                tasteComplexity = input.tasteComplexity,
                otherIndividuality = input.otherIndividuality,
                otherCautions = input.otherCautions,
                otherOverallReview = input.otherOverallReview,
            )
        val mutable = stream.value.toMutableList().apply { removeAll { it.id == id } }
        mutable.add(mapped)
        stream.value = mutable
        return id
    }

    override suspend fun deleteReview(id: ReviewId): Boolean {
        deleteFailure?.let { throw it }
        val removed = stream.value.any { review -> review.id == id }
        if (removed) {
            deletedReviewIds += id
            stream.value = stream.value.filterNot { review -> review.id == id }
        }
        return removed
    }
}

internal class ReviewFakeMasterDataRepository : MasterDataRepository {
    override suspend fun getMasterData(): MasterDataBundle =
        MasterDataBundle(
            sakeGrades =
                listOf(
                    MasterOption(value = SakeGrade.JUNMAI.name, label = "純米"),
                    MasterOption(value = SakeGrade.GINJO.name, label = "吟醸"),
                    MasterOption(value = SakeGrade.FUTSUSHU.name, label = "普通酒"),
                ),
            classifications = emptyList(),
            temperatures =
                listOf(
                    MasterOption(value = Temperature.JOON.name, label = "常温"),
                    MasterOption(value = Temperature.HANABIE.name, label = "花冷え"),
                ),
            colors =
                listOf(
                    MasterOption(value = SakeColor.CLEAR.name, label = "透明"),
                    MasterOption(value = SakeColor.AMBER.name, label = "琥珀色"),
                ),
            prefectures = emptyList(),
            intensityLevels =
                listOf(
                    MasterOption(value = IntensityLevel.VERY_WEAK.name, label = "とても弱い"),
                    MasterOption(value = IntensityLevel.WEAK.name, label = "弱い"),
                    MasterOption(value = IntensityLevel.MEDIUM.name, label = "中程度"),
                    MasterOption(value = IntensityLevel.STRONG.name, label = "強い"),
                    MasterOption(value = IntensityLevel.VERY_STRONG.name, label = "とても強い"),
                ),
            tasteLevels =
                listOf(
                    MasterOption(value = TasteLevel.VERY_WEAK.name, label = "とても弱い"),
                    MasterOption(value = TasteLevel.WEAK.name, label = "弱い"),
                    MasterOption(value = TasteLevel.MEDIUM.name, label = "中程度"),
                    MasterOption(value = TasteLevel.STRONG.name, label = "強い"),
                    MasterOption(value = TasteLevel.VERY_STRONG.name, label = "とても強い"),
                ),
            overallReviews =
                listOf(
                    MasterOption(value = OverallReview.VERY_BAD.name, label = "嫌い"),
                    MasterOption(value = OverallReview.BAD.name, label = "そうでもない"),
                    MasterOption(value = OverallReview.NEUTRAL.name, label = "普通"),
                    MasterOption(value = OverallReview.GOOD.name, label = "好き"),
                    MasterOption(value = OverallReview.VERY_GOOD.name, label = "大好き"),
                ),
            aromaCategories =
                listOf(
                    AromaCategoryMaster(
                        group = AromaGroup.FLORAL,
                        label = "フローラル",
                        items =
                            listOf(
                                MasterOption(value = Aroma.MELON.name, label = "メロン"),
                                MasterOption(value = Aroma.PEACH.name, label = "桃"),
                            ),
                    ),
                ),
        )
}

internal fun testSake(
    id: Long = TEST_SAKE_ID,
    name: String = "テスト銘柄",
    imageUri: String? = null,
): Sake =
    Sake(
        id = id,
        name = name,
        grade = SakeGrade.JUNMAI,
        imageUris = imageUri?.let(::listOf).orEmpty(),
    )

internal fun testReview(
    id: Long = TEST_REVIEW_ID,
    sakeId: Long = TEST_SAKE_ID,
    date: LocalDate = LocalDate.parse(TEST_REVIEW_DATE),
    otherOverallReview: OverallReview? = OverallReview.GOOD,
    tasteInPalateAroma: List<Aroma> = emptyList(),
): Review =
    Review(
        id = id,
        sakeId = sakeId,
        date = date,
        temperature = Temperature.JOON,
        appearanceColor = SakeColor.CLEAR,
        aromaIntensity = IntensityLevel.WEAK,
        aromaExamples = listOf(Aroma.MELON),
        tasteInPalateAroma = tasteInPalateAroma,
        tasteSweetness = TasteLevel.STRONG,
        otherOverallReview = otherOverallReview,
    )
