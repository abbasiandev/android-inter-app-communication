package dev.locationapp.feature.location.di

import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import dev.abbasian.protocol.AppLogger
import dev.locationapp.core.security.data.CryptoManager
import dev.locationapp.di.AppModule
import dev.locationapp.di.DatabaseModule
import dev.locationapp.feature.location.domain.repository.LocationRepository
import dev.locationapp.feature.location.domain.usecase.SaveLocationUseCase
import io.mockk.mockk
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [DatabaseModule::class, AppModule::class],
)
object TestDatabaseModule {
    @Provides
    @Singleton
    fun provideCryptoManager(): CryptoManager = mockk(relaxed = true)

    @Provides
    @Singleton
    fun provideAppLogger(): AppLogger = mockk(relaxed = true)

    @Provides
    @Singleton
    fun provideLocationRepository(): LocationRepository = mockk(relaxed = true)

    @Provides
    @Singleton
    fun provideSaveLocationUseCase(): SaveLocationUseCase = mockk(relaxed = true)
}
