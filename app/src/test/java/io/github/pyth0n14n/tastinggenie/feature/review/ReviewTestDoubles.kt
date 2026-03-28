package io.github.pyth0n14n.tastinggenie.feature.review

import io.github.pyth0n14n.tastinggenie.domain.model.AromaCategoryMaster
import io.github.pyth0n14n.tastinggenie.domain.model.MasterDataBundle
import io.github.pyth0n14n.tastinggenie.domain.model.MasterOption
import io.github.pyth0n14n.tastinggenie.domain.model.Review
import io.github.pyth0n14n.tastinggenie.domain.model.ReviewId
import io.github.pyth0n14n.tastinggenie.domain.model.ReviewInput
import io.github.pyth0n14n.tastinggenie.domain.model.Sake
import io.github.pyth0n14n.tastinggenie.domain.model.SakeId
import io.github.pyth0n14n.tastinggenie.domain.model.SakeInput
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

    override suspend fun getSake(id: SakeId): Sake? = stream.value.firstOrNull { it.id == id }

    override suspend fun upsertSake(input: SakeInput): SakeId {
        savedInputs.add(input)
        val id = input.id ?: ((stream.value.maxOfOrNull { it.id } ?: 0L) + 1L)
        val mapped =
            Sake(
                id = id,
                name = input.name,
                grade = input.grade,
                imageUri = input.imageUri,
            )
        val mutable = stream.value.toMutableList().apply { removeAll { it.id == id } }
        mutable.add(mapped)
        stream.value = mutable
        return id
    }
}

internal class RecordingReviewRepository(
    initial: List<Review> = emptyList(),
) : ReviewRepository {
    private val stream = MutableStateFlow(initial)
    val savedInputs = mutableListOf<ReviewInput>()

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
                color = input.color,
                viscosity = input.viscosity,
                intensity = input.intensity,
                scentTop = input.scentTop,
                scentBase = input.scentBase,
                scentMouth = input.scentMouth,
                sweet = input.sweet,
                sour = input.sour,
                bitter = input.bitter,
                umami = input.umami,
                sharp = input.sharp,
                scene = input.scene,
                dish = input.dish,
                comment = input.comment,
                review = input.review,
            )
        val mutable = stream.value.toMutableList().apply { removeAll { it.id == id } }
        mutable.add(mapped)
        stream.value = mutable
        return id
    }
}

internal class ReviewFakeMasterDataRepository : MasterDataRepository {
    override suspend fun getMasterData(): MasterDataBundle =
        MasterDataBundle(
            sakeGrades =
                listOf(
                    MasterOption(value = SakeGrade.JUNMAI.name, label = "純米"),
                    MasterOption(value = SakeGrade.GINJO.name, label = "吟醸"),
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
                    MasterOption(value = IntensityLevel.WEAK.name, label = "弱い"),
                    MasterOption(value = IntensityLevel.STRONG.name, label = "強い"),
                ),
            tasteLevels =
                listOf(
                    MasterOption(value = TasteLevel.WEAK.name, label = "弱い"),
                    MasterOption(value = TasteLevel.STRONG.name, label = "強い"),
                ),
            overallReviews =
                listOf(
                    MasterOption(value = OverallReview.GOOD.name, label = "好き"),
                    MasterOption(value = OverallReview.NEUTRAL.name, label = "普通"),
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
        imageUri = imageUri,
    )

internal fun testReview(
    id: Long = TEST_REVIEW_ID,
    sakeId: Long = TEST_SAKE_ID,
    date: LocalDate = LocalDate.parse(TEST_REVIEW_DATE),
): Review =
    Review(
        id = id,
        sakeId = sakeId,
        date = date,
        temperature = Temperature.JOON,
        color = SakeColor.CLEAR,
        intensity = IntensityLevel.WEAK,
        scentTop = listOf(Aroma.MELON),
        sweet = TasteLevel.STRONG,
        review = OverallReview.GOOD,
    )
