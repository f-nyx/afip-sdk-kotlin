package be.rlab.afip.portal

data class ApiConfig(
    val baseUrl: String,
    val endpoints: Map<String, String>
) {
    companion object {
        const val API_URL: String = "https://portalcf.cloud.afip.gob.ar"
        const val CONFIG_URL: String = "$API_URL/portal/api/config"

        const val DEBT: String = "deuda"
        const val NOTIFICATIONS: String = "intimaciones"
        const val ALL_APPS: String = "serviciosClaveFiscal"
        const val USER_APPS: String = "misServiciosClaveFiscal"
        const val APP_GET_SUBSCRIPTION: String = "consultaAdhesionServicio"
        const val APP_SUBSCRIBE: String = "realizarAdhesionServicio"
        const val APP_LOGIN: String = "tokenAccesoAServicio"
        const val DUE_DATES: String = "vencimientos"
        const val ID_INFO: String = "infoCUIT"
        const val TAXES: String = "impuestos"
        const val ACTIVITIES: String = "actividades"
        const val CONTACTS: String = "contactos"
        const val USER_INFO: String = "persona"
        const val ADDRESSES: String = "domicilios"
        const val PORTAL_INFO: String = "portal/info"

        const val PARAM_CUIT: String = "cuit"
        const val PARAM_APP_ID: String = "serviceId"
    }
}
