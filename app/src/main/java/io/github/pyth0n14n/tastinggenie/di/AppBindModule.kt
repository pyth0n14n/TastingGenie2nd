package io.github.pyth0n14n.tastinggenie.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.pyth0n14n.tastinggenie.data.master.AndroidAssetTextSource
import io.github.pyth0n14n.tastinggenie.data.master.AssetTextSource
import io.github.pyth0n14n.tastinggenie.data.repository.MasterDataRepositoryImpl
import io.github.pyth0n14n.tastinggenie.data.repository.ReviewRepositoryImpl
import io.github.pyth0n14n.tastinggenie.data.repository.SakeRepositoryImpl
import io.github.pyth0n14n.tastinggenie.domain.repository.MasterDataRepository
import io.github.pyth0n14n.tastinggenie.domain.repository.ReviewRepository
import io.github.pyth0n14n.tastinggenie.domain.repository.SakeRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppBindModule {
    @Binds
    @Singleton
    abstract fun bindAssetTextSource(impl: AndroidAssetTextSource): AssetTextSource

    @Binds
    @Singleton
    abstract fun bindSakeRepository(impl: SakeRepositoryImpl): SakeRepository

    @Binds
    @Singleton
    abstract fun bindReviewRepository(impl: ReviewRepositoryImpl): ReviewRepository

    @Binds
    @Singleton
    abstract fun bindMasterDataRepository(impl: MasterDataRepositoryImpl): MasterDataRepository
}
