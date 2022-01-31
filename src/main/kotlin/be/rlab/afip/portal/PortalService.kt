package be.rlab.afip.portal

import be.rlab.afip.portal.ApiConfig.Companion.API_URL
import be.rlab.afip.portal.ApiConfig.Companion.CONFIG_URL
import be.rlab.afip.portal.ApiConfig.Companion.PARAM_CUIT
import be.rlab.afip.support.http.HttpClientUtils
import be.rlab.afip.support.http.PersistentCookieJar
import be.rlab.afip.support.store.ObjectStore
import com.fasterxml.jackson.databind.JsonNode
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Request
import org.jsoup.nodes.Document
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/** Service to access the AFIP portal internal REST API.
 *
 * The AFIP portal is the application to manage your AFIP services. You can login into the AFIP portal
 * using your CUIT and password here: https://auth.afip.gob.ar/
 *
 * The internal API is used by the AFIP portal for rendering and running operations in the portal.
 * This API is not document, so it's subject to change, but yet this is much reliable than parsing
 * the HTML.
 */
class PortalService(
    /** Store to cache credentials. */
    private val store: ObjectStore,
    /** CUIT used as user for the login. */
    private val cuit: Long,
    /** Login password. */
    private val password: String
) {
    companion object {
        private val logger: Logger = LoggerFactory.getLogger(PortalService::class.java)
        const val SID_COOKIE: String = "AFIPSID"
    }

    private val cookieJar = PersistentCookieJar()
    private val httpClient = HttpClientUtils.newHttpClient(cookieJar)
    private lateinit var config: PortalConfig

    /** Sends a GET request to the a well-known endpoint.
     *
     * @param endpointName Name of the well-known API endpoint to call.
     * @param params HTTP request parameters.
     * @param reader The reader callback to build the result.
     * @return the result returned by the reader callback.
     */
    fun<T : Any> get(
        endpointName: String,
        vararg params: Pair<String, String>,
        reader: ((JsonNode) -> T)? = null
    ): T = loginIfRequired {
        httpClient.getJson(
            url = config.resolveEndpoint(
                endpointName,
                *(listOf(PARAM_CUIT to cuit.toString()) + params).toTypedArray()
            ).toHttpUrl(),
            reader = reader
        )
    }

    private fun login(): PortalConfig {
        cookieJar.clear()

        listOf(
            ::submitUserName,
            ::submitPassword,
            ::getAccessToken
        ).fold(openLoginPage()) { prevDoc, operation ->
            operation(prevDoc)
        }

        val authCookies = cookieJar.loadForRequest(API_URL.toHttpUrl())
        val accessToken: String = authCookies.first { cookie -> cookie.name == SID_COOKIE }.value

        return PortalConfig(
            accessToken = accessToken,
            apiConfig = loadApiConfig()
        )
    }

    /** Opens login landing page.
     */
    private fun openLoginPage(): Document {
        return httpClient.get("https://auth.afip.gob.ar/contribuyente_/login.xhtml".toHttpUrl())
    }

    private fun submitUserName(document: Document): Document {
        val submitUrl = "https://auth.afip.gob.ar${document.select("form#F1").attr("action")}"
        val viewState = document.select("[name='javax.faces.ViewState']").`val`()

        return httpClient.postForm(submitUrl.toHttpUrl(), mapOf(
            "F1" to "F1",
            "F1:username" to cuit.toString(),
            "F1:btnSiguiente" to "SIGUIENTE",
            "javax.faces.ViewState" to viewState
        ))
    }

    private fun submitPassword(document: Document): Document {
        val viewState = document.select("[name='javax.faces.ViewState']").`val`()

        return httpClient.postForm("https://auth.afip.gob.ar/contribuyente_/loginClave.xhtml".toHttpUrl(), mapOf(
            "F1" to "F1",
            "F1:captcha" to "",
            "F1:username" to cuit.toString(),
            "F1:password" to password,
            "F1:btnIngresar" to "INGRESAR",
            "javax.faces.ViewState" to viewState
        ))
    }

    private fun getAccessToken(document: Document): Document {
        val jwt: String = document.select("[name=jwt]").`val`()
        require(jwt.isNotEmpty()) { "login failed" }
        return httpClient.postForm("$API_URL/portal/login".toHttpUrl(), mapOf(
            "jwt" to jwt
        ))
    }

    private fun<T : Any> loginIfRequired(callback: () -> T): T {
        val itemId = cuit.toString()
        logger.debug("trying to read access token from cache")
        return store.read<PortalConfig>(itemId)?.content?.let { portalConfig ->
            if (isExpired(portalConfig.accessToken)) {
                logger.debug("access token expired, loading new credentials")
                loadNewAccessToken(callback)
            } else {
                logger.debug("using access token from cache")
                cookieJar.updateAuthCookie(portalConfig.accessToken)
                config = portalConfig
                callback()
            }
        } ?: loadNewAccessToken(callback)
    }

    private fun<T : Any> loadNewAccessToken(callback: () -> T): T {
        logger.debug("cache miss, loading new access token")
        config = login()

        logger.debug("saving access token")
        store.save(cuit.toString(), config)

        logger.debug("cleaning up cookie jar and setting new access token")
        cookieJar.updateAuthCookie(config.accessToken)

        return callback()
    }

    private fun isExpired(accessToken: String): Boolean {
        logger.debug("validating existing access token")
        cookieJar.updateAuthCookie(accessToken)

        val response = httpClient
            .internalClient
            .newBuilder()
            .followRedirects(false)
            .followSslRedirects(false)
            .build()
            .newCall(
                Request.Builder()
                    .url("$API_URL/portal/api/info")
                    .get()
                    .build()
            ).execute()
        return !response.isSuccessful
    }

    private fun loadApiConfig(): ApiConfig {
        return httpClient.getJson(CONFIG_URL.toHttpUrl()) { jsonConfig ->
            val jsonApiConfig = jsonConfig["api"]
            ApiConfig(
                baseUrl = jsonApiConfig["baseURL"].asText(),
                endpoints = jsonApiConfig["endpoints"].fieldNames().asSequence().associateWith { fieldName ->
                    jsonApiConfig["endpoints"][fieldName].asText()
                }
            )
        }
    }
}
