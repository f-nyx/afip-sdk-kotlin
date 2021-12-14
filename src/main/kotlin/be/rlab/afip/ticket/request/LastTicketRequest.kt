package be.rlab.afip.ticket.request

import be.rlab.afip.support.SoapRequest
import be.rlab.afip.ticket.model.TicketType

class LastTicketRequest(
    internal val pointOfSale: Int,
    internal val ticketType: TicketType
) : SoapRequest() {
    override val operationName: String = "FECompUltimoAutorizado"

    override fun build(): String {
        return """
            <soap:Envelope xmlns:soap="http://www.w3.org/2003/05/soap-envelope" xmlns:ar="http://ar.gov.afip.dif.FEV1/">
               <soap:Header/>
               <soap:Body>
                  <ar:FECompUltimoAutorizado>
                     $authHeader
                     <ar:PtoVta>$pointOfSale</ar:PtoVta>
                     <ar:CbteTipo>${ticketType.id}</ar:CbteTipo>
                  </ar:FECompUltimoAutorizado>
               </soap:Body>
            </soap:Envelope>
        """.trimIndent()
    }
}
