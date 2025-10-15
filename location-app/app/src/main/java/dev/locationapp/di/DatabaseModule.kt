package dev.locationapp.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.locationapp.core.security.CryptoManager
import dev.locationapp.data.local.LocationDao
import dev.locationapp.data.local.LocationDatabase
import dev.locationapp.data.repository.LocationRepositoryImpl
import dev.locationapp.domain.repository.LocationRepository
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideCryptoManager(): CryptoManager {
        return CryptoManager()
    }

    @Provides
    @Singleton
    fun provideLocationDatabase(
        @ApplicationContext context: Context,
        cryptoManager: CryptoManager
    ): LocationDatabase {
        val passphrase = SQLiteDatabase.getBytes(
            cryptoManager.getDatabasePassphrase().toCharArray()
        )
        val factory = SupportFactory(passphrase)

        return Room.databaseBuilder(
            context,
            LocationDatabase::class.java,
            "location_database"
        )
            .openHelperFactory(factory)
            .addMigrations(LocationDatabase.MIGRATION_1_2)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideLocationDao(database: LocationDatabase): LocationDao {
        return database.locationDao()
    }

    @Provides
    @Singleton
    fun provideLocationRepository(
        locationDao: LocationDao,
        cryptoManager: CryptoManager
    ): LocationRepository {
        return LocationRepositoryImpl(locationDao, cryptoManager)
    }
}