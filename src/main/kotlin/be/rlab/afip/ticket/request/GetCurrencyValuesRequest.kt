package be.rlab.afip.ticket.request

import be.rlab.afip.support.SoapRequest
import be.rlab.afip.ticket.TicketServiceConfig
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

class GetCurrencyValuesRequest(
    private val date: DateTime
) : SoapRequest() {
    private val formatter = DateTimeFormat.forPattern("yyyyMMdd")

    override val operationName: String = "FEXGetPARAM_MON_CON_COTIZACION"
    override val serviceName: String = TicketServiceConfig.EXPORT_SERVICE_NAME

    override fun build(): String {
        return """
            <soap:Envelope xmlns:soap="http://www.w3.org/2003/05/soap-envelope" xmlns:ar="http://ar.gov.afip.dif.fexv1/">
               <soap:Header/>
               <soap:Body>
                  <ar:FEXGetPARAM_MON_CON_COTIZACION>
                     $authHeader
                     <ar:Fecha_CTZ>${formatter.print(date)}</ar:Fecha_CTZ>
                  </ar:FEXGetPARAM_MON_CON_COTIZACION>
               </soap:Body>
            </soap:Envelope>
        """.trimIndent()
    }
}
