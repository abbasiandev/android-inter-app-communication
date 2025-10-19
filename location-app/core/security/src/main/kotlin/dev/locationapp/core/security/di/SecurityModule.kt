package dev.locationapp.core.security.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.locationapp.core.security.data.CryptoManager
import dev.locationapp.core.security.domain.ICryptoManager
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SecurityModule {
    @Binds
    @Singleton
    abstract fun bindCryptoManager(impl: CryptoManager): ICryptoManager
}
