package be.rlab.afip.apps

import be.rlab.afip.support.http.HttpClientUtils
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URLEncoder

/** Represents one of the AFIP applications available in the Portal.
 */
open class PortalApp(
    private val appsService: AppsService,
    val config: AppConfig
) {
    companion object Builder : PortalAppBuilder {
        private val logger: Logger = LoggerFactory.getLogger(PortalApp::class.java)

        override fun new(
            appsService: AppsService,
            config: AppConfig
        ): PortalApp = PortalApp(appsService, config)
    }

    val name: String = config.name
    protected val httpClient = HttpClientUtils.newHttpClient()

    protected open fun loginRequired(): Boolean {
        return true
    }

    protected fun<T : Any> loginIfRequired(callback: PortalApp.() -> T): T {
        if (loginRequired()) {
            logger.info("logging in into app: $name")
            val credentials = appsService.loadCredentials(name)

            httpClient.postForm(
                url = config.url.toHttpUrl(),
                data = mapOf(
                    "token" to URLEncoder.encode(credentials.accessToken, "utf-8"),
                    "sign" to URLEncoder.encode(credentials.sign, "utf-8")
                )
            )
        } else {
            logger.info("already logged, using existing credentials")
        }
        return callback(this)
    }
}
