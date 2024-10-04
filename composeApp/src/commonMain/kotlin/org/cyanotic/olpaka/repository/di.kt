package org.cyanotic.olpaka.repository

import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

val repositoryModule = module {
    factoryOf(::GenerateRepository) bind GenerateRepository::class
    factoryOf(::ChatRepositoryDefault) bind ChatRepository::class
    factoryOf(::ModelsRepositoryDefault) bind ModelsRepository::class
}