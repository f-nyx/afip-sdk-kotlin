package be.rlab.afip.ticket

import be.rlab.afip.auth.AuthenticationService
import be.rlab.afip.support.*
import be.rlab.afip.ticket.model.*
import be.rlab.afip.ticket.request.LastTicketRequest
import be.rlab.afip.ticket.request.ParameterTypeRequest

/** This service issues AFIP tickets.
 *
 * @see https://www.afip.gob.ar/fe/ayuda/documentos/Manual-desarrollador-V.2.21.pdf
 */
class TicketService(
    /** Service configuration. */
    config: TicketServiceConfig,
    /** Authentication service required to authorize requests. */
    authenticationService: AuthenticationService
) {
    private val client: SoapClient = SoapClient.authenticated(
        config.serviceName, config.endpoint, authenticationService
    )

    /** Creates a new ticket of type [TicketType.TICKET_C] for the specified point of sale.
     * @param pointOfSaleId Id of the point of sale to create the ticket for.
     * @param build Callback to build the ticket information.
     * @return the new ticket.
     */
    fun newTicketC(
        pointOfSaleId: Int,
        build: CreateTicketRequestBuilder.() -> Unit
    ): Ticket {
        val builder = CreateTicketRequestBuilder(this, pointOfSaleId, TicketType.TICKET_C).apply(build)

        return client.call(builder.build(), failOnError = false) {
            val remarks = select("Observaciones").map { element ->
                Remark.new(
                    code = element.number("Code").toInt(),
                    message = element.string("Msg")
                )
            }
            when(TicketStatus.of(string("FeCabResp > Resultado").trim())) {
                TicketStatus.ACCEPTED -> Ticket.accepted(
                    number = number("CAE").toLong(),
                    items = builder.items,
                    remarks = remarks,
                    errors = serviceErrors()
                )
                TicketStatus.REJECTED -> Ticket.rejected(
                    items = builder.items,
                    remarks = remarks,
                    errors = serviceErrors()
                )
            }
        }
    }

    /** Returns the last issued ticket id for a point of sale.
     * @param pointOfSaleId Id of the related point of sale.
     * @param ticketType Ticket type.
     * @return the id of the last issued ticket for the point of sale.
     */
    fun getLastTicketId(
        pointOfSaleId: Int,
        ticketType: TicketType
    ): Int {
        return client.call(LastTicketRequest(pointOfSaleId, ticketType)) {
            number("CbteNro").toInt()
        }
    }

    fun getTransactionTypes(): List<TransactionType> {
        return getParameterTypes(
            operationName = "FEParamGetTiposConcepto",
            elementName = "ConceptoTipo",
            paramTypeSupport = TransactionType
        )
    }

    fun getTicketTypes(): List<TicketType> {
        return getParameterTypes(
            operationName = "FEParamGetTiposCbte",
            elementName = "CbteTipo",
            paramTypeSupport = TicketType
        )
    }

    fun getDocumentTypes(): List<DocumentType> {
        return getParameterTypes(
            operationName = "FEParamGetTiposDoc",
            elementName = "DocTipo",
            paramTypeSupport = DocumentType
        )
    }

    fun getCurrencyTypes(): List<CurrencyType> {
        return getParameterTypes(
            operationName = "FEParamGetTiposMonedas",
            elementName = "Moneda",
            paramTypeSupport = CurrencyType
        )
    }

    fun getTaxes(): List<TaxType> {
        return getParameterTypes(
            operationName = "FEParamGetTiposTributos",
            elementName = "TributoTipo",
            paramTypeSupport = TaxType
        )
    }

    fun getIvaTypes(): List<IvaType> {
        return getParameterTypes(
            operationName = "FEParamGetTiposIva",
            elementName = "IvaTipo",
            paramTypeSupport = IvaType
        )
    }

    /** Returns the max number of items that can be included in a single ticket.
     * @return the max number of items per ticket.
     */
    fun getMaxItemsPerTicket(): Int {
        return client.call(ParameterTypeRequest("FECompTotXRequest")) {
            number("RegXReq").toInt()
        }
    }

    /** Returns the list of registered [PointOfSale]s.
     * @return the registered point of sales.
     */
    fun getPointsOfSale(): List<PointOfSale> {
        return client.call(ParameterTypeRequest("FEParamGetPtosVenta")) {
            select("PtoVenta").map { element ->
                PointOfSale.new(
                    id = element.number("Nro").toInt(),
                    ticketingMode = element.string("EmisionTipo"),
                    blocked = element.boolean("Bloqueado")
                )
            }
        }
    }

    private fun<T : ParameterType> getParameterTypes(
        operationName: String,
        elementName: String,
        paramTypeSupport: ParameterTypeSupport<T>
    ): List<T> {
        return client.call(ParameterTypeRequest(operationName)) {
            select(elementName).map { typeElement ->
                paramTypeSupport.resolve(
                    typeElement.string("Id"),
                    typeElement.string("Desc")
                )
            }
        }
    }
}
