package org.cyanotic.olpaka.core

import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val coreModule = module {
    singleOf(::ThemeState)
    singleOf(::ModelDownloadState)
    singleOf(::GoogleAnalytics) bind Analytics::class
    factoryOf(::Preferences)
}