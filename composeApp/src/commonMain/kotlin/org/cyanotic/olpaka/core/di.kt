package org.cyanotic.olpaka.core

import org.koin.dsl.module

val coreModule = module {
    single { ThemeState() }
    factory { Preferences() }
}