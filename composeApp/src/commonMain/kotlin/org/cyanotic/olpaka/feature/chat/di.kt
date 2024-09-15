package org.cyanotic.olpaka.feature.chat

import org.koin.compose.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val chatModule = module {
    viewModelOf(::ChatViewModel)
}