package fr.pralexio.perplexityintegration

import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import java.util.concurrent.Executors

class OAuthCallbackServer(
    private val onTokenReceived: (Map<String, String>) -> Unit
) {
    private var server: HttpServer? = null
    private val port = 8765

    fun start() {
        server = HttpServer.create(InetSocketAddress(port), 0)
        server?.createContext("/callback") { exchange ->
            val query = exchange.requestURI.query ?: ""
            val params = parseQueryParams(query)

            // Send success response to browser
            val response = """
                <!DOCTYPE html>
                <html>
                <head><title>Authentication Successful</title></head>
                <body style="font-family: Arial; text-align: center; padding: 50px;">
                    <h2>✓ Authentication Successful</h2>
                    <p>You can close this window and return to IntelliJ IDEA</p>
                    <script>window.close();</script>
                </body>
                </html>
            """.trimIndent()

            exchange.sendResponseHeaders(200, response.toByteArray().size.toLong())
            exchange.responseBody.use { it.write(response.toByteArray()) }

            // Notify callback
            onTokenReceived(params)

            // Stop server after callback
            stop()
        }

        server?.executor = Executors.newSingleThreadExecutor()
        server?.start()
    }

    fun stop() {
        server?.stop(0)
        server = null
    }

    private fun parseQueryParams(query: String): Map<String, String> {
        return query.split("&")
            .mapNotNull {
                val parts = it.split("=")
                if (parts.size == 2) parts[0] to parts[1] else null
            }
            .toMap()
    }

    fun getCallbackUrl(): String = "http://localhost:$port/callback"
}
