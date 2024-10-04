package org.cyanotic.olpaka.core

actual class FirebaseAnalytics : Analytics {
    actual override fun init() {
    }

    actual override fun screenView(screenName: String) {
    }

    actual override fun event(eventName: String, properties: Map<String, Any?>) {
    }

}