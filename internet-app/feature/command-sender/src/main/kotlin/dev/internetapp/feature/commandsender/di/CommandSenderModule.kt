package dev.internetapp.feature.commandsender.di

import dev.internetapp.feature.commandsender.data.repository.CommandRepositoryImpl
import dev.internetapp.feature.commandsender.domain.repository.CommandRepository
import dev.internetapp.feature.commandsender.domain.usecase.GetAllLocationsUseCase
import dev.internetapp.feature.commandsender.domain.usecase.GetLatestLocationUseCase
import dev.internetapp.feature.commandsender.domain.usecase.StartServiceUseCase
import dev.internetapp.feature.commandsender.domain.usecase.StopServiceUseCase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val commandSenderModule =
    module {
        single<CommandRepository> {
            CommandRepositoryImpl(
                context = androidContext(),
                logger = get(),
            )
        }

        factory { StartServiceUseCase(get(), get()) }
        factory { StopServiceUseCase(get(), get()) }
        factory { GetAllLocationsUseCase(get(), get()) }
        factory { GetLatestLocationUseCase(get(), get()) }
    }
