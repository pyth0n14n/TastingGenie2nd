package io.github.pyth0n14n.tastinggenie.feature.sake.list

import io.github.pyth0n14n.tastinggenie.R
import io.github.pyth0n14n.tastinggenie.domain.model.AppSettings
import io.github.pyth0n14n.tastinggenie.domain.model.MasterDataBundle
import io.github.pyth0n14n.tastinggenie.domain.model.MasterOption
import io.github.pyth0n14n.tastinggenie.domain.model.Review
import io.github.pyth0n14n.tastinggenie.domain.model.ReviewId
import io.github.pyth0n14n.tastinggenie.domain.model.ReviewInput
import io.github.pyth0n14n.tastinggenie.domain.model.Sake
import io.github.pyth0n14n.tastinggenie.domain.model.SakeDeleteResult
import io.github.pyth0n14n.tastinggenie.domain.model.SakeId
import io.github.pyth0n14n.tastinggenie.domain.model.SakeInput
import io.github.pyth0n14n.tastinggenie.domain.model.enums.SakeGrade
import io.github.pyth0n14n.tastinggenie.domain.repository.MasterDataRepository
import io.github.pyth0n14n.tastinggenie.domain.repository.ReviewRepository
import io.github.pyth0n14n.tastinggenie.domain.repository.SakeRepository
import io.github.pyth0n14n.tastinggenie.domain.repository.SettingsRepository
import io.github.pyth0n14n.tastinggenie.testutil.MainDispatcherRule
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SakeListViewModelTest {
    companion object {
        private const val DELETE_SAKE_ID = 5L
        private const val DELETE_REVIEW_COUNT = 2
        private const val SECOND_REVIEW_ID = 2L
    }

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun uiState_updatesWhenRepositoryEmitsSakes() =
        runTest {
            val repository =
                FakeSakeRepository(
                    initial =
                        listOf(
                            Sake(
                                id = 1L,
                                name = "テスト銘柄",
                                grade = SakeGrade.JUNMAI,
                            ),
                        ),
                )
            val viewModel =
                SakeListViewModel(
                    repository,
                    FakeReviewRepository(),
                    FakeMasterDataRepository(),
                    FakeSettingsRepository(),
                )
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertEquals(1, state.sakes.size)
            assertEquals("テスト銘柄", state.sakes.first().name)
            assertEquals("純米", state.gradeLabels[SakeGrade.JUNMAI.name])
            assertEquals(true, state.showImagePreview)
            assertEquals(null, state.error)
        }

    @Test
    fun uiState_setsErrorWhenRepositoryFails() =
        runTest {
            val repository = FailingObserveSakeRepository()
            val viewModel =
                SakeListViewModel(
                    repository,
                    FakeReviewRepository(),
                    FakeMasterDataRepository(),
                    FakeSettingsRepository(),
                )
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertNotNull(state.error)
            assertEquals(R.string.error_load_sakes, state.error?.messageResId)
        }

    @Test
    fun uiState_updatesWhenImagePreviewSettingChanges() =
        runTest {
            val settingsRepository = FakeSettingsRepository()
            val viewModel =
                SakeListViewModel(
                    FakeSakeRepository(initial = emptyList()),
                    FakeReviewRepository(),
                    FakeMasterDataRepository(),
                    settingsRepository,
                )
            advanceUntilIdle()

            settingsRepository.updateShowImagePreview(enabled = false)
            advanceUntilIdle()

            assertEquals(false, viewModel.uiState.value.showImagePreview)
        }

    @Test
    fun requestDeleteSake_populatesConfirmationStateWithReviewCount() =
        runTest {
            val viewModel =
                SakeListViewModel(
                    FakeSakeRepository(
                        initial =
                            listOf(
                                Sake(
                                    id = DELETE_SAKE_ID,
                                    name = "削除対象",
                                    grade = SakeGrade.JUNMAI,
                                    imageUri = "file:///images/sakes/$DELETE_SAKE_ID.jpg",
                                ),
                            ),
                    ),
                    FakeReviewRepository(
                        initial =
                            listOf(
                                testReview(id = 1L, sakeId = DELETE_SAKE_ID),
                                testReview(
                                    id = SECOND_REVIEW_ID,
                                    sakeId = DELETE_SAKE_ID,
                                    date = java.time.LocalDate.parse("2026-03-15"),
                                ),
                            ),
                    ),
                    FakeMasterDataRepository(),
                    FakeSettingsRepository(),
                )
            advanceUntilIdle()

            viewModel.requestDeleteSake(DELETE_SAKE_ID)
            advanceUntilIdle()

            val pending = viewModel.uiState.value.pendingDeleteSake
            assertEquals("削除対象", pending?.sakeName)
            assertEquals(DELETE_REVIEW_COUNT, pending?.reviewCount)
            assertEquals(true, pending?.hasImage)
        }

    @Test
    fun confirmDeleteSake_deletesSakeAndClearsDialog() =
        runTest {
            val sakeRepository =
                FakeSakeRepository(
                    initial =
                        listOf(
                            Sake(
                                id = DELETE_SAKE_ID,
                                name = "削除対象",
                                grade = SakeGrade.JUNMAI,
                            ),
                        ),
                )
            val viewModel =
                SakeListViewModel(
                    sakeRepository,
                    FakeReviewRepository(),
                    FakeMasterDataRepository(),
                    FakeSettingsRepository(),
                )
            advanceUntilIdle()

            viewModel.requestDeleteSake(DELETE_SAKE_ID)
            advanceUntilIdle()
            viewModel.confirmDeleteSake()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals(emptyList<Sake>(), state.sakes)
            assertEquals(null, state.pendingDeleteSake)
            assertEquals(null, state.deleteError)
            assertEquals(listOf(DELETE_SAKE_ID), sakeRepository.deletedIds)
        }

    @Test
    fun confirmDeleteSake_whenCleanupFails_setsInlineErrorButKeepsDeleteCommitted() =
        runTest {
            val sakeRepository =
                FakeSakeRepository(
                    initial =
                        listOf(
                            Sake(
                                id = DELETE_SAKE_ID,
                                name = "削除対象",
                                grade = SakeGrade.JUNMAI,
                            ),
                        ),
                    deleteResult =
                        SakeDeleteResult(
                            isDeleted = true,
                            hasImageCleanupError = true,
                            imageCleanupErrorCauseKey = "cleanup failed",
                        ),
                )
            val viewModel =
                SakeListViewModel(
                    sakeRepository,
                    FakeReviewRepository(),
                    FakeMasterDataRepository(),
                    FakeSettingsRepository(),
                )
            advanceUntilIdle()

            viewModel.requestDeleteSake(DELETE_SAKE_ID)
            advanceUntilIdle()
            viewModel.confirmDeleteSake()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals(emptyList<Sake>(), state.sakes)
            assertEquals(R.string.error_delete_sake_image_cleanup, state.deleteError?.messageResId)
        }

    @Test
    fun confirmDeleteSake_doesNotExposeCancellationAsDeleteError() =
        runTest {
            val sakeRepository =
                FakeSakeRepository(
                    initial =
                        listOf(
                            Sake(
                                id = DELETE_SAKE_ID,
                                name = "削除対象",
                                grade = SakeGrade.JUNMAI,
                            ),
                        ),
                    deleteFailure = CancellationException("cancelled"),
                )
            val viewModel =
                SakeListViewModel(
                    sakeRepository,
                    FakeReviewRepository(),
                    FakeMasterDataRepository(),
                    FakeSettingsRepository(),
                )
            advanceUntilIdle()

            viewModel.requestDeleteSake(DELETE_SAKE_ID)
            advanceUntilIdle()
            viewModel.confirmDeleteSake()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals(null, state.deleteError)
            assertEquals(1, state.sakes.size)
        }

    @Test
    fun requestDeleteSake_ignoresStaleResultWhenLaterTapFinishesFirst() =
        runTest {
            val firstSakeId = DELETE_SAKE_ID
            val secondSakeId = DELETE_SAKE_ID + 1
            val reviewRepository = SequencedReviewRepository()
            val viewModel =
                SakeListViewModel(
                    FakeSakeRepository(
                        initial =
                            listOf(
                                Sake(
                                    id = firstSakeId,
                                    name = "先行タップ",
                                    grade = SakeGrade.JUNMAI,
                                ),
                                Sake(
                                    id = secondSakeId,
                                    name = "後勝ち",
                                    grade = SakeGrade.GINJO,
                                ),
                            ),
                    ),
                    reviewRepository,
                    FakeMasterDataRepository(),
                    FakeSettingsRepository(),
                )
            advanceUntilIdle()

            viewModel.requestDeleteSake(firstSakeId)
            viewModel.requestDeleteSake(secondSakeId)
            advanceUntilIdle()

            reviewRepository.complete(
                secondSakeId,
                listOf(testReview(id = SECOND_REVIEW_ID, sakeId = secondSakeId)),
            )
            advanceUntilIdle()
            reviewRepository.complete(
                firstSakeId,
                listOf(
                    testReview(id = 1L, sakeId = firstSakeId),
                    testReview(id = SECOND_REVIEW_ID, sakeId = firstSakeId),
                ),
            )
            advanceUntilIdle()

            val pending = viewModel.uiState.value.pendingDeleteSake
            assertEquals(secondSakeId, pending?.sakeId)
            assertEquals("後勝ち", pending?.sakeName)
            assertEquals(1, pending?.reviewCount)
        }
}

private class FakeSakeRepository(
    initial: List<Sake>,
    private val deleteResult: SakeDeleteResult = SakeDeleteResult(isDeleted = true),
    private val deleteFailure: Throwable? = null,
) : SakeRepository {
    private val stream = MutableStateFlow(initial)
    val deletedIds = mutableListOf<SakeId>()

    override fun observeSakes(): Flow<List<Sake>> = stream

    override suspend fun getSake(id: SakeId): Sake? = stream.value.firstOrNull { it.id == id }

    override suspend fun upsertSake(input: SakeInput): SakeId {
        val id = input.id ?: ((stream.value.maxOfOrNull { it.id } ?: 0L) + 1L)
        val mapped =
            Sake(
                id = id,
                name = input.name,
                grade = input.grade,
            )
        val mutable = stream.value.toMutableList().apply { removeAll { it.id == id } }
        mutable.add(mapped)
        stream.value = mutable.sortedBy { it.id }
        return id
    }

    override suspend fun deleteSake(id: SakeId): SakeDeleteResult {
        deleteFailure?.let { throw it }
        if (!deleteResult.isDeleted) {
            return deleteResult
        }
        deletedIds += id
        stream.value = stream.value.filterNot { sake -> sake.id == id }
        return deleteResult
    }
}

private class FailingObserveSakeRepository : SakeRepository {
    override fun observeSakes(): Flow<List<Sake>> = flow { throw IllegalStateException("failed") }

    override suspend fun getSake(id: SakeId): Sake? = null

    override suspend fun upsertSake(input: SakeInput): SakeId = 1L

    override suspend fun deleteSake(id: SakeId): SakeDeleteResult = SakeDeleteResult(isDeleted = false)
}

private class FakeReviewRepository(
    initial: List<Review> = emptyList(),
) : ReviewRepository {
    private val stream = MutableStateFlow(initial)

    override fun observeReviews(sakeId: SakeId): Flow<List<Review>> =
        flow { emit(stream.value.filter { review -> review.sakeId == sakeId }) }

    override suspend fun getReview(id: ReviewId): Review? = stream.value.firstOrNull { review -> review.id == id }

    override suspend fun upsertReview(input: ReviewInput): ReviewId = 1L

    override suspend fun deleteReview(id: ReviewId): Boolean = false
}

private class SequencedReviewRepository : ReviewRepository {
    private val responses = mutableMapOf<SakeId, CompletableDeferred<List<Review>>>()

    override fun observeReviews(sakeId: SakeId): Flow<List<Review>> =
        flow {
            emit(responseFor(sakeId).await())
        }

    override suspend fun getReview(id: ReviewId): Review? = null

    override suspend fun upsertReview(input: ReviewInput): ReviewId = 1L

    override suspend fun deleteReview(id: ReviewId): Boolean = false

    fun complete(
        sakeId: SakeId,
        reviews: List<Review>,
    ) {
        responseFor(sakeId).complete(reviews)
    }

    private fun responseFor(sakeId: SakeId): CompletableDeferred<List<Review>> =
        responses.getOrPut(sakeId) { CompletableDeferred() }
}

private class FakeMasterDataRepository : MasterDataRepository {
    override suspend fun getMasterData(): MasterDataBundle =
        MasterDataBundle(
            sakeGrades =
                listOf(
                    MasterOption(value = SakeGrade.JUNMAI.name, label = "純米"),
                    MasterOption(value = SakeGrade.GINJO.name, label = "吟醸"),
                ),
            classifications = emptyList(),
            temperatures = emptyList(),
            colors = emptyList(),
            prefectures = emptyList(),
            intensityLevels = emptyList(),
            tasteLevels = emptyList(),
            overallReviews = emptyList(),
            aromaCategories = emptyList(),
        )
}

private class FakeSettingsRepository : SettingsRepository {
    private val stream = MutableStateFlow(AppSettings())

    override fun observeSettings(): Flow<AppSettings> = stream

    override suspend fun updateShowHelpHints(enabled: Boolean) {
        stream.value = stream.value.copy(showHelpHints = enabled)
    }

    override suspend fun updateShowImagePreview(enabled: Boolean) {
        stream.value = stream.value.copy(showImagePreview = enabled)
    }
}

private fun testReview(
    id: Long,
    sakeId: Long,
    date: java.time.LocalDate = java.time.LocalDate.parse("2026-03-14"),
): Review =
    Review(
        id = id,
        sakeId = sakeId,
        date = date,
    )
