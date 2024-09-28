package org.cyanotic.olpaka.feature.models

import org.koin.compose.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val modelsModule = module {
    viewModelOf(::ModelsViewModel)
    viewModelOf(::ModelsAddModelViewModel)
}