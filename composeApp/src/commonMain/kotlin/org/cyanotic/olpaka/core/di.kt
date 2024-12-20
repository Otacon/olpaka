package org.cyanotic.olpaka.core

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val coreModule = module {

    singleOf(::ThemeState)
    singleOf(::FirebaseAnalytics) bind Analytics::class

    factoryOf(::PreferencesDefault) bind Preferences::class
    factoryOf(::StringResourcesDefault) bind StringResources::class
    factoryOf(::DownloadStatsCalculatorDefault) bind DownloadStatsCalculator::class
    factory<CoroutineDispatcher> { Dispatchers.Default }

}