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
            if (stored.isNotEmpty()) {
                log.info("Perplexity token loaded from PasswordSafe (length=${stored.length}).")
                cached = stored
                return@synchronized stored
            }

            val legacy = PerplexitySettings.getInstance().sessionToken
            if (legacy.isEmpty()) {
                cached = ""
                return@synchronized ""
            }

            // Legacy XML token exists but PasswordSafe is empty.
            // Try to migrate, but only clear the XML if we can verify persistence.
            tryMigrateLegacy(legacy)
            cached = legacy
            log.info("Perplexity token loaded from legacy XML storage (length=${legacy.length}).")
            legacy
        }
    }

    fun setToken(token: String) {
        synchronized(this) {
            cached = token
            val legacy = PerplexitySettings.getInstance()

            if (PasswordSafe.instance.isMemoryOnly) {
                // PasswordSafe will lose this on restart. Keep XML as the persistent store.
                legacy.sessionToken = token
                writeSecure(token) // in-memory, helps in-session lookups
                log.info("Perplexity token saved to legacy XML (PasswordSafe is memory-only).")
                return@synchronized
            }

            writeSecure(token)

            if (token.isEmpty()) {
                if (legacy.sessionToken.isNotEmpty()) legacy.sessionToken = ""
                return@synchronized
            }

            // Verify the secure store actually round-trips before clearing the XML fallback.
            val verified = readSecure() == token
            if (verified) {
                if (legacy.sessionToken.isNotEmpty()) legacy.sessionToken = ""
                log.info("Perplexity token saved to PasswordSafe (verified, length=${token.length}).")
            } else {
                legacy.sessionToken = token
                log.warn("PasswordSafe write was not readable back; keeping XML fallback (length=${token.length}).")
            }
        }
    }

    private fun tryMigrateLegacy(legacyToken: String) {
        if (PasswordSafe.instance.isMemoryOnly) {
            log.info("Skipping legacy migration: PasswordSafe is in memory-only mode; XML stays as the store.")
            return
        }
        try {
            writeSecure(legacyToken)
            val verified = readSecure() == legacyToken
            if (verified) {
                PerplexitySettings.getInstance().sessionToken = ""
                log.info("Migrated Perplexity session token from XML to PasswordSafe.")
            } else {
                log.warn("PasswordSafe round-trip failed during migration; keeping XML token.")
            }
        } catch (e: Exception) {
            log.warn("Migration to PasswordSafe failed; keeping XML token.", e)
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
        try {
            val attrs = credentialAttributes()
            if (token.isEmpty()) {
                PasswordSafe.instance.set(attrs, null)
            } else {
                PasswordSafe.instance.set(attrs, Credentials(USERNAME, token))
            }
        } catch (e: Exception) {
            log.warn("Failed to write Perplexity session token to PasswordSafe", e)
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
