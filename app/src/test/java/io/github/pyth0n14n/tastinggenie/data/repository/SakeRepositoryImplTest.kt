package io.github.pyth0n14n.tastinggenie.data.repository

import io.github.pyth0n14n.tastinggenie.domain.model.SakeInput
import io.github.pyth0n14n.tastinggenie.domain.model.enums.SakeGrade
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SakeRepositoryImplTest {
    @Test
    fun upsertSake_insertsAndObserves() = runTest {
        val dao = FakeSakeDao()
        val repository = SakeRepositoryImpl(dao, StandardTestDispatcher(testScheduler))

        val createdId = repository.upsertSake(
            SakeInput(
                name = "テスト酒",
                grade = SakeGrade.JUNMAI,
            ),
        )

        val loaded = repository.getSake(createdId)
        val observed = repository.observeSakes().first { it.isNotEmpty() }

        assertNotNull(loaded)
        assertEquals("テスト酒", loaded?.name)
        assertEquals(1, observed.size)
    }

    @Test
    fun upsertSake_updatesExistingEntity() = runTest {
        val dao = FakeSakeDao()
        val repository = SakeRepositoryImpl(dao, StandardTestDispatcher(testScheduler))

        val createdId = repository.upsertSake(
            SakeInput(
                name = "更新前",
                grade = SakeGrade.JUNMAI,
            ),
        )
        repository.upsertSake(
            SakeInput(
                id = createdId,
                name = "更新後",
                grade = SakeGrade.GINJO,
            ),
        )

        val loaded = repository.getSake(createdId)

        assertEquals("更新後", loaded?.name)
        assertEquals(SakeGrade.GINJO, loaded?.grade)
    }
}
