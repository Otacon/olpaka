package org.cyanotic.olpaka.feature.chat

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val chatModule = module {
    viewModel { ChatViewModel(get(), get()) }
}