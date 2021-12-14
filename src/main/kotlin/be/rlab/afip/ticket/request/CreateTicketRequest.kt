package be.rlab.afip.ticket.request

import be.rlab.afip.support.SoapRequest
import be.rlab.afip.ticket.model.TicketItem
import be.rlab.afip.ticket.model.TicketType
import org.joda.time.format.DateTimeFormat

/** This request issues a ticket.
 */
class CreateTicketRequest(
    val pointOfSaleId: Int,
    val ticketType: TicketType,
    val items: List<TicketItem>
) : SoapRequest() {
    private val formatter = DateTimeFormat.forPattern("yyyyMMdd")

    companion object {
        fun ticketC(
            pointOfSaleId: Int,
            items: List<TicketItem>
        ): CreateTicketRequest = CreateTicketRequest(
            pointOfSaleId = pointOfSaleId,
            ticketType = TicketType.TICKET_C,
            items = items
        )
    }

    override val operationName: String = "FECAESolicitar"

    override fun build(): String {
        return """
            <soap:Envelope xmlns:soap="http://www.w3.org/2003/05/soap-envelope" xmlns:ar="http://ar.gov.afip.dif.FEV1/">
               <soap:Header/>
               <soap:Body>
                  <ar:FECAESolicitar>
                     $authHeader
                     <ar:FeCAEReq>
                        <ar:FeCabReq>
                           <ar:CantReg>${items.size}</ar:CantReg>
                           <ar:PtoVta>${pointOfSaleId}</ar:PtoVta>
                           <ar:CbteTipo>${ticketType.id}</ar:CbteTipo>
                        </ar:FeCabReq>
                        <ar:FeDetReq>
                           ${buildItems()}
                        </ar:FeDetReq>
                     </ar:FeCAEReq>
                  </ar:FECAESolicitar>
               </soap:Body>
            </soap:Envelope>
        """.trimIndent()
    }

    private fun buildItems(): String {
        return items.joinToString("\n") { item -> """
           <ar:FECAEDetRequest>
              <ar:Concepto>${item.transactionType.id}</ar:Concepto>
              <ar:DocTipo>${item.document.type.id}</ar:DocTipo>
              <ar:DocNro>${item.document.number}</ar:DocNro>
              <ar:CbteDesde>${item.ticketIdFrom}</ar:CbteDesde>
              <ar:CbteHasta>${item.ticketIdTo}</ar:CbteHasta>
              <ar:CbteFch>${formatter.print(item.issuedAt)}</ar:CbteFch>
              <ar:ImpTotal>${item.totalValue}</ar:ImpTotal>
              <ar:ImpTotConc>${item.netValueWithoutTaxes}</ar:ImpTotConc>
              <ar:ImpNeto>${item.netValue}</ar:ImpNeto>
              <ar:ImpOpEx>${item.exemptValue}</ar:ImpOpEx>
              <ar:ImpTrib>${item.totalTaxes}</ar:ImpTrib>
              <ar:ImpIVA>${item.totalIva}</ar:ImpIVA>
              <ar:FchServDesde>${formatter.print(item.serviceStartDate)}</ar:FchServDesde>
              <ar:FchServHasta>${formatter.print(item.serviceEndDate)}</ar:FchServHasta>
              <ar:FchVtoPago>${formatter.print(item.paymentDueDate)}</ar:FchVtoPago>
              <ar:MonId>${item.currencyType.id}</ar:MonId>
              <ar:MonCotiz>1</ar:MonCotiz>
           </ar:FECAEDetRequest>
        """.trimIndent() }
    }
}
