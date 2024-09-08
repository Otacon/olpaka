package org.cyanotic.olpaka.feature.models

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val modelsModule = module {
    viewModel { ModelsViewModel(get()) }
}