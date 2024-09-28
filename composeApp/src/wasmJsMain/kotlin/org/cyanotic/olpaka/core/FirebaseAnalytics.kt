@file:JsModule("firebase/analytics")
package org.cyanotic.olpaka.core

external fun getAnalytics(app: JsAny) : JsAny

external fun logEvent(analytics: JsAny, eventName: String)

external fun logEvent(analytics: JsAny, eventName: String, properties: JsAny)