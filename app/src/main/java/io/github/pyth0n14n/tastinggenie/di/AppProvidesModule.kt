package io.github.pyth0n14n.tastinggenie.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.pyth0n14n.tastinggenie.data.local.AppDatabase
import io.github.pyth0n14n.tastinggenie.data.local.dao.ReviewDao
import io.github.pyth0n14n.tastinggenie.data.local.dao.SakeDao
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json

@Module
@InstallIn(SingletonComponent::class)
object AppProvidesModule {
    private const val DATABASE_NAME = "tasting_genie.db"

    @Provides
    @Singleton
    fun provideJson(): Json = Json { ignoreUnknownKeys = true }

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME).build()
    }

    @Provides
    fun provideSakeDao(database: AppDatabase): SakeDao = database.sakeDao()

    @Provides
    fun provideReviewDao(database: AppDatabase): ReviewDao = database.reviewDao()

    @Provides
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
}
