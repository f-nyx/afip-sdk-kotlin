package be.rlab.afip.ticket.request

import be.rlab.afip.support.SoapRequest
import be.rlab.afip.ticket.TicketServiceConfig

/** This request retrieves AFIP parameters like ticket types and transaction types.
 * These requests do not accept parameters, they only require the operation name.
 */
class ParameterTypeRequest(
    override val serviceName: String,
    override val operationName: String
) : SoapRequest() {

    private val soapActionBase: String = when(serviceName) {
        TicketServiceConfig.LOCAL_SERVICE_NAME -> TicketServiceConfig.LOCAL_SOAP_ACTION_BASE
        TicketServiceConfig.EXPORT_SERVICE_NAME -> TicketServiceConfig.EXPORT_SOAP_ACTION_BASE
        else -> throw RuntimeException("unsupported service: $serviceName")
    }

    companion object {
        fun local(operationName: String): ParameterTypeRequest = ParameterTypeRequest(
            serviceName = TicketServiceConfig.LOCAL_SERVICE_NAME,
            operationName = operationName
        )

        fun export(operationName: String): ParameterTypeRequest = ParameterTypeRequest(
            serviceName = TicketServiceConfig.EXPORT_SERVICE_NAME,
            operationName = operationName
        )
    }

    override fun build(): String {
        return """
            <soap:Envelope xmlns:soap="http://www.w3.org/2003/05/soap-envelope" xmlns:ar="$soapActionBase">
               <soap:Header/>
               <soap:Body>
                  <ar:$operationName>
                     $authHeader
                  </ar:$operationName>
               </soap:Body>
            </soap:Envelope>
        """.trimIndent()
    }
}
