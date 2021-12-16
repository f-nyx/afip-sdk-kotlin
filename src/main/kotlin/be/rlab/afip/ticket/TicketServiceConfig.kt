package be.rlab.afip.ticket

import be.rlab.afip.config.Environment

/** Configuration for the [TicketService].
 */
data class TicketServiceConfig(
    /** Service name. */
    val serviceName: String,
    /** Endpoint to call the service. */
    val endpoint: String,
    /** Base URL for building the SOAPAction header required by this service. */
    val soapActionBase: String
) {
    companion object {
        const val LOCAL_SERVICE_NAME = "wsfe"
        const val EXPORT_SERVICE_NAME = "wsfex"
        const val LOCAL_SOAP_ACTION_BASE = "http://ar.gov.afip.dif.FEV1/"
        const val EXPORT_SOAP_ACTION_BASE = "http://ar.gov.afip.dif.fexv1/"

        fun local(environment: Environment) = TicketServiceConfig(
            serviceName = LOCAL_SERVICE_NAME,
            endpoint = environment.resolveValue(
                test = "https://wswhomo.afip.gov.ar/wsfev1/service.asmx",
                production = "https://servicios1.afip.gov.ar/wsfev1/service.asmx"
            ),
            soapActionBase = LOCAL_SOAP_ACTION_BASE
        )

        fun export(environment: Environment) = TicketServiceConfig(
            serviceName = EXPORT_SERVICE_NAME,
            endpoint = environment.resolveValue(
                test = "https://wswhomo.afip.gov.ar/wsfexv1/service.asmx",
                production = "https://servicios1.afip.gov.ar/wsfexv1/service.asmx"
            ),
            soapActionBase = EXPORT_SOAP_ACTION_BASE
        )
    }
}
