package org.cyanotic.olpaka.network

import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.koin.dsl.module

val networkModule = module {
    single {
        Json {
            ignoreUnknownKeys = true
            isLenient = true
        }
    }

    single {
        HttpClient {
            install(ContentNegotiation) {
                json(get())
            }
        }
    }
}