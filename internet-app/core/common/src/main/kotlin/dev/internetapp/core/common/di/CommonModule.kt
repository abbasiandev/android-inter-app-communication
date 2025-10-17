package dev.internetapp.core.common.di

import dev.abbasian.protocol.AppLogger
import dev.abbasian.protocol.TimberLogger
import org.koin.dsl.module

val commonModule =
    module {
        single<AppLogger> { TimberLogger() }
    }
