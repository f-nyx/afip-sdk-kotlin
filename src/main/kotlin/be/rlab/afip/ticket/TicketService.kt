package be.rlab.afip.ticket

import be.rlab.afip.support.*
import be.rlab.afip.ticket.model.*
import be.rlab.afip.ticket.request.GetCurrencyValuesRequest
import be.rlab.afip.ticket.request.LastExportTicketRequest
import be.rlab.afip.ticket.request.LastTicketRequest
import be.rlab.afip.ticket.request.ParameterTypeRequest
import org.joda.time.DateTime

/** This service issues AFIP tickets.
 *
 * @see https://www.afip.gob.ar/fe/ayuda/documentos/Manual-desarrollador-V.2.21.pdf
 */
class TicketService(private val client: SoapClient) {
    /** Creates a new ticket of type [TicketType.TicketC] for the specified point of sale.
     * @param pointOfSaleId Id of the point of sale to create the ticket for.
     * @param build Callback to build the ticket information.
     * @return the new ticket.
     */
    fun newTicketC(
        pointOfSaleId: Int,
        build: CreateTicketRequestBuilder.() -> Unit
    ): Ticket<TicketItem> {
        val builder = CreateTicketRequestBuilder(this, pointOfSaleId, TicketType.TicketC).apply(build)

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

    /** Creates a new ticket of type [TicketType.TicketE] for the specified point of sale.
     * @param pointOfSaleId Id of the point of sale to create the ticket for.
     * @param build Callback to build the ticket information.
     * @return the new ticket.
     */
    fun newTicketE(
        pointOfSaleId: Int,
        build: CreateTicketRequestBuilder.() -> Unit
    ): Ticket<ExportTicketItem> {
        val builder = CreateTicketRequestBuilder(this, pointOfSaleId, TicketType.TicketE).apply(build)

        return client.call(builder.buildExport(), failOnError = false) {
            when(TicketStatus.of(string("FEXResultAuth > Resultado").trim())) {
                TicketStatus.ACCEPTED -> Ticket.accepted(
                    number = number("Cae").toLong(),
                    items = builder.exportItems,
                    remarks = emptyList(),
                    errors = serviceErrors()
                )
                TicketStatus.REJECTED -> Ticket.rejected(
                    items = builder.exportItems,
                    remarks = emptyList(),
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
        return if (ticketType.export) {
            client.call(LastExportTicketRequest(pointOfSaleId, ticketType)) {
                number("Cbte_nro").toInt()
            }
        } else {
            client.call(LastTicketRequest(pointOfSaleId, ticketType)) {
                number("CbteNro").toInt()
            }
        }
    }

    fun getTransactionTypes(): List<TransactionType> {
        return getParameterTypes(
            serviceName = TicketServiceConfig.LOCAL_SERVICE_NAME,
            operationName = "FEParamGetTiposConcepto",
            elementName = "ConceptoTipo",
            paramTypeSupport = TransactionType
        )
    }

    fun getTicketTypes(): List<TicketType> {
        return getParameterTypes(
            serviceName = TicketServiceConfig.LOCAL_SERVICE_NAME,
            operationName = "FEParamGetTiposCbte",
            elementName = "CbteTipo",
            paramTypeSupport = TicketType
        ) + getParameterTypes(
            serviceName = TicketServiceConfig.EXPORT_SERVICE_NAME,
            operationName = "FEXGetPARAM_Cbte_Tipo",
            elementName = "ClsFEXResponse_Cbte_Tipo",
            paramTypeSupport = TicketType,
            idElement = "Cbte_Id",
            descElement = "Cbte_Ds"
        )
    }

    fun getDocumentTypes(): List<DocumentType> {
        return getParameterTypes(
            serviceName = TicketServiceConfig.LOCAL_SERVICE_NAME,
            operationName = "FEParamGetTiposDoc",
            elementName = "DocTipo",
            paramTypeSupport = DocumentType
        )
    }

    fun getCurrencyTypes(): List<CurrencyType> {
        return getParameterTypes(
            serviceName = TicketServiceConfig.LOCAL_SERVICE_NAME,
            operationName = "FEParamGetTiposMonedas",
            elementName = "Moneda",
            paramTypeSupport = CurrencyType
        )
    }

    fun getCurrencyValues(date: DateTime): List<CurrencyValue> {
        val currencies = getCurrencyTypes().associateBy { currencyType -> currencyType.id }
        val currencyValues: Map<String, Double> = client.call(GetCurrencyValuesRequest(date)) {
            select("ClsFEXResponse_Mon_CON_Cotizacion").associate { element ->
                element.string("Mon_Id") to element.number("Mon_ctz").toDouble()
            }
        }
        return currencyValues.map { (currencyId, value) ->
            CurrencyValue(currencies.getValue(currencyId), value, date)
        }
    }

    fun getTaxes(): List<TaxType> {
        return getParameterTypes(
            serviceName = TicketServiceConfig.LOCAL_SERVICE_NAME,
            operationName = "FEParamGetTiposTributos",
            elementName = "TributoTipo",
            paramTypeSupport = TaxType
        )
    }

    fun getIvaTypes(): List<IvaType> {
        return getParameterTypes(
            serviceName = TicketServiceConfig.LOCAL_SERVICE_NAME,
            operationName = "FEParamGetTiposIva",
            elementName = "IvaTipo",
            paramTypeSupport = IvaType
        )
    }

    fun getUnitTypes(): List<UnitType> {
        return getParameterTypes(
            serviceName = TicketServiceConfig.EXPORT_SERVICE_NAME,
            operationName = "FEXGetPARAM_UMed",
            elementName = "ClsFEXResponse_UMed",
            paramTypeSupport = UnitType,
            idElement = "Umed_Id",
            descElement = "Umed_Ds"
        )
    }

    fun getLanguages(): List<Language> {
        return getParameterTypes(
            serviceName = TicketServiceConfig.EXPORT_SERVICE_NAME,
            operationName = "FEXGetPARAM_Idiomas",
            elementName = "ClsFEXResponse_Idi",
            paramTypeSupport = Language,
            idElement = "Idi_Id",
            descElement = "Idi_Ds"
        )
    }

    fun getIncoterms(): List<IncotermType> {
        return getParameterTypes(
            serviceName = TicketServiceConfig.EXPORT_SERVICE_NAME,
            operationName = "FEXGetPARAM_Incoterms",
            elementName = "ClsFEXResponse_Inc",
            paramTypeSupport = IncotermType,
            idElement = "Inc_Id",
            descElement = "Inc_Ds"
        )
    }

    fun getLocations(): List<Location> {
        return getParameterTypes(
            serviceName = TicketServiceConfig.EXPORT_SERVICE_NAME,
            operationName = "FEXGetPARAM_DST_pais",
            elementName = "ClsFEXResponse_DST_pais",
            paramTypeSupport = Location,
            idElement = "DST_Codigo",
            descElement = "DST_Ds"
        ).sortedBy { it.description }
    }

    /** Returns the list of legal entities for countries and territories.
     *
     * Each country and territory has its own CUIT in AFIP. For emitting export tickets, AFIP requires both
     * the destination country or territory CUIT and the destination person's legal id.
     *
     * This is an incomplete list since not all responses from the FEXGetPARAM_DST_CUIT service matches a
     * [Location].
     *
     * @return the list of locations legal entities.
     */
    fun getLocationEntities(): List<LocationEntity> {
        val locations = getLocations()

        return client.call(ParameterTypeRequest(TicketServiceConfig.EXPORT_SERVICE_NAME, "FEXGetPARAM_DST_CUIT")) {
            select("ClsFEXResponse_DST_cuit").mapNotNull { element ->
                val cuit: Long = element.number("DST_CUIT").toLong()
                val description = element.string("DST_Ds")
                val type: EntityType = EntityType.of(description.substringAfterLast("-").trim())

                locations.find { location ->
                    location.description.lowercase() == description.substringBefore("-").lowercase().trim()
                }?.let { location ->
                    LocationEntity(location, type, cuit)
                }
            }
        }
    }

    /** Returns the max number of items that can be included in a single ticket.
     * @return the max number of items per ticket.
     */
    fun getMaxItemsPerTicket(): Int {
        return client.call(ParameterTypeRequest.local("FECompTotXRequest")) {
            number("RegXReq").toInt()
        }
    }

    /** Returns the list of registered [PointOfSale]s.
     * @return the registered point of sales.
     */
    fun getPointsOfSale(): List<PointOfSale> {
        return client.call(ParameterTypeRequest.local("FEParamGetPtosVenta")) {
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
        serviceName: String,
        operationName: String,
        elementName: String,
        paramTypeSupport: ParameterTypeSupport<T>,
        idElement: String = "Id",
        descElement: String = "Desc"
    ): List<T> {
        return client.call(ParameterTypeRequest(serviceName, operationName)) {
            select(elementName).mapNotNull { typeElement ->
                if (typeElement.hasContent(idElement) && typeElement.hasContent(descElement)) {
                    paramTypeSupport.resolve(
                        typeElement.string(idElement),
                        typeElement.string(descElement)
                    )
                } else {
                    null
                }
            }
        }
    }
}
