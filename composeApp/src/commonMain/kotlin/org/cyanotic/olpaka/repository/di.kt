package org.cyanotic.olpaka.repository

import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val repositoryModule = module {
    factoryOf(::GenerateRepository)
    factoryOf(::ChatRepository)
    factoryOf(::ModelsRepository)
}