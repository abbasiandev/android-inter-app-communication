package dev.locationapp.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.abbasian.protocol.AppLogger
import dev.abbasian.protocol.TimberLogger
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppLogger(): AppLogger {
        return TimberLogger()
    }
}
