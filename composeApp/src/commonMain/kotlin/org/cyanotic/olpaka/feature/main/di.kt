package org.cyanotic.olpaka.feature.main

import org.koin.compose.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val mainModule = module {
    viewModelOf(::MainViewModel)
}