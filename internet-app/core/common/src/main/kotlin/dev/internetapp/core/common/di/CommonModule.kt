package dev.internetapp.core.common.di

import dev.abbasian.protocol.domain.logger.AppLogger
import dev.abbasian.protocol.domain.logger.TimberLogger
import org.koin.dsl.module

val commonModule =
    module {
        single<AppLogger> { TimberLogger() }
    }
