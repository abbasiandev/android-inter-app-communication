package dev.internetapp.feature.responsedisplay.presentation.di

import dev.internetapp.feature.responsedisplay.presentation.viewmodel.CommandViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val responseDisplayModule =
    module {
        viewModel {
            CommandViewModel(
                startServiceUseCase = get(),
                stopServiceUseCase = get(),
                getAllLocationsUseCase = get(),
                getLatestLocationUseCase = get(),
                logger = get(),
                analytics = get(),
            )
        }
    }
