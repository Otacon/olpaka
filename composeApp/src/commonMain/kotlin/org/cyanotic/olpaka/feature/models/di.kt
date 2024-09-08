package org.cyanotic.olpaka.feature.models

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val modelsModule = module {
    viewModelOf(::ModelsViewModel)
}