package org.cyanotic.olpaka.feature.models

import org.koin.compose.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

val modelsModule = module {

    viewModelOf(::ModelsViewModel)
    viewModelOf(::ModelsAddModelViewModel)

    factoryOf(::DownloadStatsFormatterDefault) bind DownloadStatsFormatter::class

}