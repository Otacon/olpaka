package org.cyanotic.olpaka.repository

import org.koin.dsl.module

val repositoryModule = module {
    factory<GenerateRepository> { GenerateRepository(get(), get()) }
    factory<ModelsRepository> { ModelsRepository(get(), get()) }
}