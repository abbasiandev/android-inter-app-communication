package dev.internetapp.di

import dev.abbasian.protocol.AppLogger
import dev.abbasian.protocol.TimberLogger
import dev.internetapp.data.CommandRepositoryImpl
import dev.internetapp.domain.repository.CommandRepository
import dev.internetapp.domain.usecase.GetAllLocationsUseCase
import dev.internetapp.domain.usecase.GetLatestLocationUseCase
import dev.internetapp.domain.usecase.StartServiceUseCase
import dev.internetapp.domain.usecase.StopServiceUseCase
import dev.internetapp.presentation.CommandViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    single<AppLogger> { TimberLogger() }

    single<CommandRepository> {
        CommandRepositoryImpl(
            context = androidContext(),
            logger = get()
        )
    }

    factory { StartServiceUseCase(get(), get()) }
    factory { StopServiceUseCase(get(), get()) }
    factory { GetAllLocationsUseCase(get(), get()) }
    factory { GetLatestLocationUseCase(get(), get()) }

    viewModel {
        CommandViewModel(
            startServiceUseCase = get(),
            stopServiceUseCase = get(),
            getAllLocationsUseCase = get(),
            getLatestLocationUseCase = get(),
            logger = get()
        )
    }
}