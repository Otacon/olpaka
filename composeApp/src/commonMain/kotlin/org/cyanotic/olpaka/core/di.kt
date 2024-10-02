package org.cyanotic.olpaka.core

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val coreModule = module {
    singleOf(::ThemeState)
    singleOf(::ModelDownloadStateDefault) bind ModelDownloadState::class
    singleOf(::FirebaseAnalytics) bind Analytics::class
    factoryOf(::PreferencesDefault) bind Preferences::class
    factory<CoroutineDispatcher> { Dispatchers.Default }
}