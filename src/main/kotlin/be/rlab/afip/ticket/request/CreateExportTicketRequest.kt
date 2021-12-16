package be.rlab.afip.ticket.request

import be.rlab.afip.support.SoapRequest
import be.rlab.afip.ticket.TicketServiceConfig
import be.rlab.afip.ticket.model.*
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

/** This request issues an export ticket.
 */
class CreateExportTicketRequest(
    private val ticketType: TicketType,
    private val pointOfSaleId: Int,
    /** A custom identifier for this ticket. */
    private val customIdentifier: Int,
    /** Id of the next ticket to generate. */
    private val ticketId: Int,
    /** Date this ticket was issued. */
    private val issueDate: DateTime,
    /** Transaction type this ticket is created for. */
    private val transactionType: TransactionType,
    /** Destination country or territory. */
    private val targetLocation: Location,
    /** Destination country or territory legal information. */
    private val locationEntity: LocationEntity,
    /** Recipient's full name. */
    private val customerFullName: String,
    /** Recipient's full address. */
    private val customerFullAddress: String,
    /** Recipient's legal id. */
    private val customerLegalId: String,
    /** Currency value at the moment the ticket is issued. */
    private val currency: CurrencyValue,
    /** Language of this ticket. */
    private val language: Language,
    /** Payment method description. */
    private val paymentMethod: String,
    /** Payment date. */
    private val paymentDate: DateTime,
    /** Commercial terms, if applies. */
    private val incoterms: IncotermType?,
    /** List of items in this ticket. */
    private val items: List<ExportTicketItem>
) : SoapRequest() {
    private val formatter = DateTimeFormat.forPattern("yyyyMMdd")

    override val operationName: String = "FEXAuthorize"
    override val serviceName: String = TicketServiceConfig.EXPORT_SERVICE_NAME
    /** Total price, it's the sum of all total prices in items. */
    private val totalValue: Double = items.sumOf { item -> item.totalValue }

    override fun build(): String {
        return """
            <soap:Envelope xmlns:soap="http://www.w3.org/2003/05/soap-envelope" xmlns:ar="http://ar.gov.afip.dif.fexv1/">
               <soap:Header/>
               <soap:Body>
                  <ar:FEXAuthorize>
                     $authHeader
                     
                     <ar:Cmp>
                        <ar:Id>$customIdentifier</ar:Id>
                        <ar:Fecha_cbte>${formatter.print(issueDate)}</ar:Fecha_cbte>
                        <ar:Cbte_Tipo>${ticketType.id}</ar:Cbte_Tipo>
                        <ar:Punto_vta>$pointOfSaleId</ar:Punto_vta>
                        <ar:Cbte_nro>$ticketId</ar:Cbte_nro>
                        <ar:Tipo_expo>${transactionType.id}</ar:Tipo_expo>
                        <ar:Dst_cmp>${targetLocation.id}</ar:Dst_cmp>
                        <ar:Cliente>$customerFullName</ar:Cliente>
                        <ar:Cuit_pais_cliente>${locationEntity.cuit}</ar:Cuit_pais_cliente>
                        <ar:Domicilio_cliente>$customerFullAddress</ar:Domicilio_cliente>
                        <ar:Id_impositivo>$customerLegalId</ar:Id_impositivo>
                        <ar:Moneda_Id>${currency.currency.id}</ar:Moneda_Id>
                        <ar:Moneda_ctz>${currency.value}</ar:Moneda_ctz>
                        <ar:Imp_total>$totalValue</ar:Imp_total>
                        <ar:Forma_pago>$paymentMethod</ar:Forma_pago>
                        <ar:Permiso_existente></ar:Permiso_existente>
                        ${incoterms?.let { """
                            <ar:Incoterms>${incoterms.id}</ar:Incoterms>
                            <ar:Incoterms_Ds>${incoterms.description}</ar:Incoterms_Ds>
                        """.trimIndent() 
                        } ?: ""}
                        <ar:Idioma_cbte>${language.id}</ar:Idioma_cbte>
                        <ar:Items>
                            ${buildItems()}
                        </ar:Items>
                        <ar:Fecha_pago>${formatter.print(paymentDate)}</ar:Fecha_pago>
                     </ar:Cmp>
                  </ar:FEXAuthorize>
               </soap:Body>
            </soap:Envelope>
        """.trimIndent()
    }

    private fun buildItems(): String {
        return items.joinToString("\n") { item -> """
           <ar:Item>
              <ar:Pro_codigo>${item.productCode}</ar:Pro_codigo>
              <ar:Pro_ds>${item.productDescription}</ar:Pro_ds>
              <ar:Pro_qty>${item.quantity}</ar:Pro_qty>
              <ar:Pro_umed>${item.unit.id}</ar:Pro_umed>
              <ar:Pro_precio_uni>${item.unitValue}</ar:Pro_precio_uni>
              <ar:Pro_bonificacion>${item.discount}</ar:Pro_bonificacion>
              <ar:Pro_total_item>${item.totalValue}</ar:Pro_total_item>
           </ar:Item>
        """.trimIndent() }
    }
}
