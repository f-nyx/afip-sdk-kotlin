package be.rlab.afip.apps

import be.rlab.afip.apps.certs.CertificateApp
import be.rlab.afip.portal.ApiConfig
import be.rlab.afip.portal.PortalService
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/** Service to access AFIP applications.
 */
class AppsService(
    /** Portal client required to authenticate applications. */
    private val portalService: PortalService
) {
    companion object {
        private val logger: Logger = LoggerFactory.getLogger(AppsService::class.java)
        const val ERROR_NOT_REGISTERED: String =
            "The required application is not registered for your user. You can register the application in the " +
            "following link (you need to be logged first):" +
            "https://serviciosweb.afip.gob.ar/claveFiscal/adminRel/main.aspx"
    }

    private val appBuilders: Map<String, PortalAppBuilder> = mapOf(
        "wsass" to CertificateApp.Builder
    )

    fun listApps(): List<PortalApp> {
        logger.info("retrieving app list")
        return portalService.get(ApiConfig.ALL_APPS) { jsonApps ->
            jsonApps.map { jsonApp ->
                val serviceName: String = jsonApp["serviceName"].asText()
                val config = AppConfig(
                    name = serviceName,
                    type = jsonApp["serviceType"].asText(),
                    description = jsonApp["descriptiontext"].asText(),
                    url = jsonApp["url"].asText(),
                    level = jsonApp["nivel"].asInt(),
                    organization = jsonApp["orgName"].asText(),
                    authVersion = jsonApp["ticketversion"].asText(),
                    publicKey = jsonApp["publickey"]?.asText(),
                    canRemove = jsonApp["borrable"].asBoolean(),
                    canDelegate = jsonApp["delegable"].asBoolean(),
                    canRequest = jsonApp["pedible"].asBoolean(),
                    isVisible = jsonApp["visible"].asBoolean(),
                    isDefault = jsonApp["default"].asBoolean(),
                    isOnline = !jsonApp["presencial"].asBoolean()
                )
                val builder = appBuilders[serviceName] ?: PortalApp.Builder
                builder.new(appsService = this, config = config)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun<T : PortalApp> openApp(appName: String): T {
        return listApps().find { app -> app.name == appName }?.let { app -> app as T }
            ?: throw RuntimeException(ERROR_NOT_REGISTERED)
    }

    inline fun<reified T : PortalApp> openApp(): T {
        return listApps().find { app -> app is T }?.let { app -> app as T }
            ?: throw RuntimeException(ERROR_NOT_REGISTERED)
    }

    fun loadCredentials(appName: String): Credentials {
        return portalService.get(ApiConfig.APP_LOGIN, ApiConfig.PARAM_APP_ID to appName) { jsonToken ->
            Credentials(accessToken = jsonToken["token"].asText(), sign = jsonToken["sign"].asText())
        }
    }
}
