package org.cyanotic.olpaka.feature.settings

import org.koin.compose.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val settingsModule = module {
    viewModelOf(::SettingsViewModel)
}