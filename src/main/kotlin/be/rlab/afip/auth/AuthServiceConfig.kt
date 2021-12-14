package be.rlab.afip.auth

import be.rlab.afip.config.Environment

/** Configuration for the [AuthenticationService].
 */
data class AuthServiceConfig(
    /** Service name. */
    val serviceName: String,
    /** CUIT number of the represented person.
     * @TODO(nyx): check if this can be parametrized per request for different people using the same certificate.
     */
    val cuit: Long,
    /** AFIP's distinguished name to identify the service. */
    val dn: String,
    /** Endpoint to call the service. */
    val endpoint: String
) {
    companion object {
        const val SERVICE_NAME = "wsaa"

        fun new(
            environment: Environment,
            cuit: Long
        ) = AuthServiceConfig(
            serviceName = SERVICE_NAME,
            cuit = cuit,
            endpoint = environment.resolveValue(
                test = "https://wsaahomo.afip.gov.ar/ws/services/LoginCms",
                production = "https://wsaa.afip.gov.ar/ws/services/LoginCms"
            ),
            dn = environment.resolveValue(
                test = "cn=wsaahomo,o=afip,c=ar,serialNumber=CUIT 33693450239",
                production = "cn=wsaa,o=afip,c=ar,serialNumber=CUIT 33693450239"
            )
        )
    }
}
