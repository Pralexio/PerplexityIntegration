package fr.pralexio.perplexityintegration

import com.intellij.credentialStore.CredentialAttributes
import com.intellij.credentialStore.Credentials
import com.intellij.credentialStore.generateServiceName
import com.intellij.ide.passwordSafe.PasswordSafe
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger

@Service(Service.Level.APP)
class PerplexityCredentialStore {

    private val log = Logger.getInstance(PerplexityCredentialStore::class.java)

    @Volatile
    private var cached: String? = null

    fun getToken(): String {
        cached?.let { return it }
        return synchronized(this) {
            cached?.let { return@synchronized it }
            val stored = readSecure()
            val token = if (stored.isNotEmpty()) {
                stored
            } else {
                migrateLegacyTokenIfNeeded()
            }
            cached = token
            token
        }
    }

    fun setToken(token: String) {
        synchronized(this) {
            writeSecure(token)
            cached = token
            // Ensure the legacy XML field never lingers after a save.
            val legacy = PerplexitySettings.getInstance()
            if (legacy.sessionToken.isNotEmpty()) {
                legacy.sessionToken = ""
            }
        }
    }

    private fun migrateLegacyTokenIfNeeded(): String {
        val legacy = PerplexitySettings.getInstance()
        val legacyToken = legacy.sessionToken
        if (legacyToken.isEmpty()) return ""
        return try {
            writeSecure(legacyToken)
            legacy.sessionToken = ""
            log.info("Migrated Perplexity session token from XML storage to PasswordSafe.")
            legacyToken
        } catch (e: Exception) {
            log.warn("Failed to migrate Perplexity session token to PasswordSafe; keeping legacy value.", e)
            legacyToken
        }
    }

    private fun readSecure(): String {
        return try {
            PasswordSafe.instance.get(credentialAttributes())?.getPasswordAsString().orEmpty()
        } catch (e: Exception) {
            log.warn("Failed to read Perplexity session token from PasswordSafe", e)
            ""
        }
    }

    private fun writeSecure(token: String) {
        val attrs = credentialAttributes()
        if (token.isEmpty()) {
            PasswordSafe.instance.set(attrs, null)
        } else {
            PasswordSafe.instance.set(attrs, Credentials(USERNAME, token))
        }
    }

    private fun credentialAttributes(): CredentialAttributes =
        CredentialAttributes(generateServiceName(SERVICE_NAME, KEY))

    companion object {
        private const val SERVICE_NAME = "Perplexity Integration"
        private const val KEY = "session-token"
        private const val USERNAME = "perplexity"

        fun getInstance(): PerplexityCredentialStore = service()
    }
}
