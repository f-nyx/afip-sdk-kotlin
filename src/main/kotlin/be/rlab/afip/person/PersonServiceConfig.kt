package be.rlab.afip.person

import be.rlab.afip.config.Environment

/** Configuration for the [PersonService].
 */
data class PersonServiceConfig(
    /** Service name. */
    val serviceName: String,
    /** Endpoint to call the service. */
    val endpoint: String,
    /** Base URL for building the SOAPAction header required by this service. */
    val soapActionBase: String
) {
    companion object {
        const val SERVICE_NAME = "ws_sr_padron_a4"
        const val LOCAL_SOAP_ACTION_BASE = "http://ar.gov.afip.dif.FEV1/"

        fun create(environment: Environment) = PersonServiceConfig(
            serviceName = SERVICE_NAME,
            endpoint = environment.resolveValue(
                test = "https://awshomo.afip.gov.ar/sr-padron/webservices/personaServiceA4",
                production = "https://aws.afip.gov.ar/sr-padron/webservices/personaServiceA4"
            ),
            soapActionBase = LOCAL_SOAP_ACTION_BASE
        )
    }
}
