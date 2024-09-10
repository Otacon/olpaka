package org.cyanotic.olpaka.network

import io.ktor.client.*
import io.ktor.client.plugins.*
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
            install(HttpTimeout) {
                requestTimeoutMillis = REQUEST_TIMEOUT
                connectTimeoutMillis = CONNECT_TIMEOUT
                socketTimeoutMillis = SOCKET_TIMEOUT
            }
            install(ContentNegotiation) {
                json(get())
            }
        }
    }
}

const val SECOND = 1_000L
const val MINUTE = 60 * SECOND
const val REQUEST_TIMEOUT = 10 * MINUTE
const val CONNECT_TIMEOUT = 10 * MINUTE
const val SOCKET_TIMEOUT = 10 * MINUTE
