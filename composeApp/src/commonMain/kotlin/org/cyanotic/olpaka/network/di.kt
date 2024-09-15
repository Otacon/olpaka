package org.cyanotic.olpaka.network

import io.github.aakira.napier.Napier
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.factoryOf
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
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        Napier.i(tag = "Network") { message }
                    }

                }
                level = LogLevel.BODY
            }
        }
    }

    single {
        EndpointProvider(
            baseUrl = parseUrl(EndpointProvider.DEFAULT_OLLAMA_API_URL)!!,
        )
    }

    factoryOf(::OllamaRestClient)
}

const val SECOND = 1_000L
const val MINUTE = 60 * SECOND
const val REQUEST_TIMEOUT = 10 * MINUTE
const val CONNECT_TIMEOUT = 10 * MINUTE
const val SOCKET_TIMEOUT = 10 * MINUTE

