package org.cyanotic.olpaka.core

expect class FirebaseAnalytics() {
    fun init()

    fun screenView(screenName: String)

    fun event(eventName: String, properties: Map<String,Any?>)
}
