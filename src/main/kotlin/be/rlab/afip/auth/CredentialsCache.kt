package be.rlab.afip.auth

import be.rlab.afip.auth.model.Credentials
import be.rlab.afip.support.store.ObjectStore
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/** Cache to store AFIP credentials.
 *
 * AFIP credentials are valid for 12 hours. The authentication service returns an error if new credentials
 * are required before the old credentials expired. This cache takes care of the expiration time rules and
 * allows loading new credentials only when it's required.
 *
 * Implementations can define the persistence strategy to save the credentials while they are active.
 */
class CredentialsCache(
    /** Store used to save and retrieve credentials. */
    private val store: ObjectStore,
    /** Indicates whether the cache is enabled.
     * If not, it will always cause a miss.
     */
    private val enabled: Boolean = true
) {
    companion object {
        private val logger: Logger = LoggerFactory.getLogger(CredentialsCache::class.java)
    }

    /** Retrieves the credentials for a service from the cache, and loads the credentials if
     * they don't exist or expired.
     * @param serviceName Service to load credentials for.
     * @param loadCallback Callback to load the credentials if the cache misses.
     * @return The required credentials.
     */
    fun loadIfRequired(
        serviceName: String,
        loadCallback: () -> Credentials
    ): Credentials {
        logger.debug("trying to read credentials from cache for service: $serviceName")
        return if (enabled) {
            store.read<Credentials>(serviceName)?.content?.let { credentials ->
                if (isExpired(credentials)) {
                    logger.debug("credentials expired, loading new credentials")
                    store.save(serviceName, loadCallback(), emptyMap())
                } else {
                    logger.debug("using credentials from cache")
                    credentials
                }
            } ?: loadNewCredentials(serviceName, loadCallback)
        } else {
            loadNewCredentials(serviceName, loadCallback)
        }
    }

    private fun loadNewCredentials(
        serviceName: String,
        loadCallback: () -> Credentials
    ): Credentials {
        logger.debug("cache miss for service: $serviceName, loading new credentials")
        val newCredentials = loadCallback()
        logger.debug("saving credentials")
        return store.save(serviceName, newCredentials)
    }

    private fun isExpired(credentials: Credentials): Boolean {
        return System.currentTimeMillis() > credentials.expiresAt
    }
}
