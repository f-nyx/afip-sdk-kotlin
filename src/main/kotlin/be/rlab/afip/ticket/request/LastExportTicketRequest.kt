package be.rlab.afip.ticket.request

import be.rlab.afip.support.SoapRequest
import be.rlab.afip.ticket.TicketServiceConfig
import be.rlab.afip.ticket.model.TicketType

/** Returns the last export ticket id.
 * This is required to calculate the next ticket id, since it's not automatic.
 */
class LastExportTicketRequest(
    internal val pointOfSale: Int,
    internal val ticketType: TicketType
) : SoapRequest() {
    override val operationName: String = "FEXGetLast_CMP"
    override val serviceName: String = TicketServiceConfig.EXPORT_SERVICE_NAME

    override fun build(): String {
        return """
            <soap:Envelope xmlns:soap="http://www.w3.org/2003/05/soap-envelope" xmlns:ar="http://ar.gov.afip.dif.fexv1/">
               <soap:Header/>
               <soap:Body>
                  <ar:FEXGetLast_CMP>
                     ${authHeader { """
                         <ar:Pto_venta>$pointOfSale</ar:Pto_venta>
                         <ar:Cbte_Tipo>${ticketType.id}</ar:Cbte_Tipo>
                     """.trimIndent() }}
                  </ar:FEXGetLast_CMP>
               </soap:Body>
            </soap:Envelope>
        """.trimIndent()
    }
}
