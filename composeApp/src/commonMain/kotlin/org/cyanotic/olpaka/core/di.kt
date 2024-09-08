package org.cyanotic.olpaka.core

import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val coreModule = module {
    singleOf(::ThemeState)
    factoryOf(::Preferences)
}