package org.cyanotic.olpaka.core

import com.cyanotic.olpaka.BuildKonfig
import io.github.aakira.napier.Napier
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

actual class FirebaseAnalytics {

    private var analytics: JsAny? = null

    actual fun init() {

        try {
            val configurationJson = BuildKonfig.firebaseWebConfigJson
            Napier.d("Firebase web configuration $configurationJson")
            val firebaseConfiguration = Json.decodeFromString<FirebaseWebConfig>(configurationJson)
            val configuration = createConfiguration(
                apiKey = firebaseConfiguration.apiKey,
                authDomain = firebaseConfiguration.authDomain,
                projectId = firebaseConfiguration.projectId,
                storageBucket = firebaseConfiguration.storageBucket,
                messagingSenderId = firebaseConfiguration.messagingSenderId,
                appId = firebaseConfiguration.appId,
                measurementId = firebaseConfiguration.measurementId
            )
            val app = initializeApp(configuration)
            analytics = getAnalytics(app)
        } catch (e: Exception){
            Napier.w("Exception while configuring analytics")
        } catch (e: Error){
            Napier.w("Error while configuring analytics")
        }


    }

    actual fun screenView(screenName: String) {
        analytics?.let { logEvent(it, screenName) }

    }

    actual fun event(eventName: String, properties: Map<String, Any?>) {
        analytics?.let { logEvent(it, eventName, properties.toJsAny()) }
    }

}

fun Map<String, Any?>.toJsAny(): JsAny {
    val jsMap = createPropertyMap()
    this.forEach { (key, value) ->
        addProperty(jsMap, key, value?.toString() ?: "")
    }
    return jsMap
}

fun createPropertyMap(): JsAny = js(
    """({})"""
)

fun addProperty(propertyMap: JsAny, key: String, value: String): Unit = js(
    """{ propertyMap[key] = value; }"""
)

fun createConfiguration(
    apiKey: String,
    authDomain: String,
    projectId: String,
    storageBucket: String,
    messagingSenderId: String,
    appId: String,
    measurementId: String,
): JsAny = js(
    """
    ({
        apiKey: apiKey,
        authDomain: authDomain,
        projectId: projectId,
        storageBucket: storageBucket,
        messagingSenderId: messagingSenderId,
        appId: appId,
        measurementId: measurementId
    })
"""
)

@Serializable
data class FirebaseWebConfig(
    @SerialName("apiKey") val apiKey: String,
    @SerialName("authDomain") val authDomain: String,
    @SerialName("projectId") val projectId: String,
    @SerialName("storageBucket") val storageBucket: String,
    @SerialName("messagingSenderId") val messagingSenderId: String,
    @SerialName("appId") val appId: String,
    @SerialName("measurementId") val measurementId: String,
)