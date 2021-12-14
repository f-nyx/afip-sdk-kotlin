package be.rlab.afip.ticket

import be.rlab.afip.config.Environment

/** Configuration for the [TicketService].
 */
data class TicketServiceConfig(
    /** Service name. */
    val serviceName: String,
    /** Endpoint to call the service. */
    val endpoint: String
) {
    companion object {
        const val SERVICE_NAME = "wsfe"

        fun new(environment: Environment) = TicketServiceConfig(
            serviceName = SERVICE_NAME,
            endpoint = environment.resolveValue(
                test = "https://wswhomo.afip.gov.ar/wsfev1/service.asmx",
                production = "https://servicios1.afip.gov.ar/wsfev1/service.asmx"
            )
        )
    }
}
