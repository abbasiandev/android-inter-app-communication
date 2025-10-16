package dev.locationapp.di

import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import dev.abbasian.protocol.AppLogger
import dev.locationapp.core.security.CryptoManager
import dev.locationapp.domain.repository.LocationRepository
import dev.locationapp.domain.usecase.SaveLocationUseCase
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
    fun provideCryptoManager(): CryptoManager {
        return mockk(relaxed = true)
    }

    @Provides
    @Singleton
    fun provideAppLogger(): AppLogger {
        return mockk(relaxed = true)
    }

    @Provides
    @Singleton
    fun provideLocationRepository(): LocationRepository {
        return mockk(relaxed = true)
    }

    @Provides
    @Singleton
    fun provideSaveLocationUseCase(): SaveLocationUseCase {
        return mockk(relaxed = true)
    }
}
