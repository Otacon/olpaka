package org.cyanotic.olpaka.core

interface Analytics {
    fun init()

    fun screenView(screenName: String)

    fun event(eventName: String, properties: Map<String,Any?> = emptyMap())
}

expect class FirebaseAnalytics() : Analytics {
    override fun init()

    override fun screenView(screenName: String)

    override fun event(eventName: String, properties: Map<String,Any?>)
}
