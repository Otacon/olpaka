package org.cyanotic.olpaka.network

class EndpointProvider(
    var scheme: String,
    var host: String,
    var port: Int,
    var baseUrl: String,
) {

    fun generateUrl(endpoint: String): String {
        return "$scheme://$host:$port$baseUrl$endpoint"
    }

}