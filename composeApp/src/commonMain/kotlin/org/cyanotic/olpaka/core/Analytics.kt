package org.cyanotic.olpaka.core

import io.github.aakira.napier.Napier
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.serialization.*
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.coroutines.CoroutineContext

interface Analytics {

    fun init(
        clientId: String,
        measurementId: String,
        apiSecret: String,
        debug: Boolean = false,
    )

    fun trackScreenView(screenName: String)

    fun trackEvent(name: String, params: Map<String, Any?>)

}

class GoogleAnalytics(
    private val client: HttpClient,
) : Analytics, CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = SupervisorJob() + Dispatchers.Default

    private var clientId: String? = null
    private var measurementId: String? = null
    private var apiSecret: String? = null
    private var debug: Boolean = false
    private var session: String = Clock.System.now().toEpochMilliseconds().toString()
    private var startTime: Long = Clock.System.now().toEpochMilliseconds()

    override fun init(clientId: String, measurementId: String, apiSecret: String, debug: Boolean) {
        this.clientId = clientId
        this.measurementId = measurementId
        this.apiSecret = apiSecret
        this.debug = debug
    }

    override fun trackScreenView(screenName: String) {
        track(
            EVENT_NAME_SCREEN_VIEW,
            mapOf("screen_name" to screenName)
        )
    }

    override fun trackEvent(name: String, params: Map<String, Any?>) {
        track(name, params)
    }

    private fun track(eventName: String, params: Map<String, Any?>) {
        Napier.d(tag = "Analytics", message = "Sending \"$eventName\" event with params: $params")
        val clientId = this.clientId
        val measurementId = this.measurementId
        val apiSecret = this.apiSecret

        if (clientId == null || measurementId == null || apiSecret == null) {
            Napier.w(tag = "Analytics", message = "Analytics are not initialised.")
            return
        }
        val engagementTimeMs = Clock.System.now().toEpochMilliseconds() - startTime
        val enrichedParams = params +
                ("session_id" to session) +
                ("engagement_time_msec" to engagementTimeMs) +
                ("debug_mode" to true)

        val jsonParams = buildJsonObject {
            enrichedParams.forEach{ (key, value) ->
                when(value){
                    is String -> put(key, value)
                    is Boolean -> put(key, value)
                    is Int -> put(key, value)
                    is Long -> put(key, value)
                    is Float -> put(key, value)
                    is Double -> put(key, value)
                    null -> Unit
                    else -> throw IllegalArgumentException("Unable to add param $key")
                }
            }
        }

        launch {
            val event = Event(
                clientId = clientId,
                events = listOf(
                    EventSpec(
                        name = eventName,
                        params = jsonParams
                    )
                ),
                userProperties = UserProperty(
                    value = UserPropertyValue("My device")
                )
            )
            val url = GA_URL

            client.post(url) {
                url {
                    parameters.append("measurement_id", measurementId)
                    parameters.append("api_secret", apiSecret)
                }
                contentType(ContentType.Application.Json)
                setBody(event)
            }
            Napier.i(tag = "Analytics", message = "\"$eventName\" sent!")
        }
    }

    companion object {

        private const val GA_URL = "https://www.google-analytics.com/mp/collect"
        private const val GA_DEBUG_URL = "https://www.google-analytics.com/debug/mp/collect"
        private const val EVENT_NAME_SCREEN_VIEW = "screen_view"
    }

}

@Serializable
data class Event(
    @SerialName("client_id") val clientId: String,
    @SerialName("events") val events: List<EventSpec>,
    @SerialName("user_properties") val userProperties: UserProperty,
)

@Serializable
data class EventSpec(
    @SerialName("name") val name: String,

    @SerialName("params")
    val params: JsonObject,
)

@Serializable
data class UserProperty(
    @SerialName("device_name") val value: UserPropertyValue,
)

@Serializable
data class UserPropertyValue(
    @SerialName("value") val value: String,
)