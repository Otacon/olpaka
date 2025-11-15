package org.cyanotic.olpaka.core

import com.cyanotic.olpaka.BuildKonfig
import io.github.aakira.napier.Napier
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.coroutines.CoroutineContext
import kotlin.time.ExperimentalTime


actual class FirebaseAnalytics : CoroutineScope, KoinComponent, Analytics {

    override val coroutineContext: CoroutineContext
        get() = SupervisorJob() + Dispatchers.Default

    private val client by inject<HttpClient>()
    private val preferences by inject<Preferences>()

    private var clientId: String? = null
    private var measurementId: String? = null
    private var apiSecret: String? = null

    @OptIn(ExperimentalTime::class)
    private var session: String = Clock.System.now().toEpochMilliseconds().toString()
    @OptIn(ExperimentalTime::class)
    private var startTime: Long = Clock.System.now().toEpochMilliseconds()

    actual override fun init() {
        this.measurementId = BuildKonfig.analyticsMeasurementId
        this.apiSecret = BuildKonfig.analyticsApiSecret
        this.clientId = preferences.analyticsClientId
    }

    actual override fun screenView(screenName: String) {
        track(
            EVENT_NAME_SCREEN_VIEW,
            mapOf("screen_name" to screenName)
        )
    }

    actual override fun event(eventName: String, properties: Map<String,Any?>) {
        track(eventName, properties)
    }

    @OptIn(ExperimentalTime::class)
    private fun track(eventName: String, params: Map<String, Any?>) {
        Napier.d(tag = "Analytics", message = "Sending \"$eventName\" with params: $params")
        val clientId = this.clientId
        val measurementId = this.measurementId
        val apiSecret = this.apiSecret

        if (clientId.isNullOrBlank() || measurementId.isNullOrBlank() || apiSecret.isNullOrBlank()) {
            Napier.w(tag = "Analytics", message = "Analytics are not initialised.")
            return
        }
        val engagementTimeMs = Clock.System.now().toEpochMilliseconds() - startTime
        val enrichedParams = params +
                ("session_id" to session) +
                ("engagement_time_msec" to engagementTimeMs)

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
                    value = UserPropertyValue("Desktop")
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
            Napier.d(tag = "Analytics", message = "\"$eventName\" sent!")
        }
    }

    companion object {

        private const val GA_URL = "https://www.google-analytics.com/mp/collect"
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