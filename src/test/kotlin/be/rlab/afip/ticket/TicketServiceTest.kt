package be.rlab.afip.ticket

import be.rlab.afip.config.Environment
import be.rlab.afip.support.ServiceTestSupport.getRequest
import be.rlab.afip.support.ServiceTestSupport.withClient
import be.rlab.afip.support.TestSoapClient
import be.rlab.afip.ticket.model.*
import be.rlab.afip.ticket.request.LastTicketRequest
import be.rlab.afip.ticket.request.ParameterTypeRequest
import com.nhaarman.mockitokotlin2.mock
import org.joda.time.DateTime
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream


class TicketServiceTest {
    data class ParameterTypeSource(
        val operationName: String,
        val values: List<*>,
        val exec: TicketService.() -> List<*>
    )

    companion object {
        const val SERVICE_NAME: String = "ticket"

        @JvmStatic
        fun parameterTypesSource(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(ParameterTypeSource("FEParamGetTiposConcepto", TransactionType.all()) { getTransactionTypes() }),
//                Arguments.of(ParameterTypeSource("FEParamGetTiposCbte", TicketType.all()) { getTicketTypes() }),
                Arguments.of(ParameterTypeSource("FEParamGetTiposTributos", TaxType.all()) { getTaxes() }),
                Arguments.of(ParameterTypeSource("FEParamGetTiposIva", IvaType.all()) { getIvaTypes() }),
                Arguments.of(ParameterTypeSource("FEParamGetTiposDoc", DocumentType.all()) { getDocumentTypes() }),
                Arguments.of(ParameterTypeSource("FEParamGetTiposMonedas", CurrencyType.all()) { getCurrencyTypes() }),
                Arguments.of(ParameterTypeSource("FEParamGetPtosVenta", listOf(
                    PointOfSale.new(0, "CAE", blocked = false),
                    PointOfSale.new(1, "CAE", blocked = true)
                )) { getPointsOfSale() }),
            )
        }
    }

    @Test
    fun getLastTicketId() {
        // prepare
        val operationName = "FECompUltimoAutorizado"
        val service = newService(operationName, "ok")

        // act
        assert(service.getLastTicketId(3, TicketType.TicketC) == 25)

        // assert
        val request: LastTicketRequest = getRequest(service, "FECompUltimoAutorizado")
        assert(request.operationName == "FECompUltimoAutorizado")
        assert(request.pointOfSale == 3)
        assert(request.ticketType == TicketType.TicketC)
    }

    @ParameterizedTest
    @MethodSource("parameterTypesSource")
    fun getParameters(source: ParameterTypeSource) {
        val service = newService(source.operationName, "ok")

        // act
        val values = source.exec(service)

        // assert
        assert(source.values.all { parameterType -> values.contains(parameterType) })

        val request: ParameterTypeRequest = getRequest(service, source.operationName)
        assert(request.operationName == source.operationName)
    }

    @Test
    fun getMaxItemsPerTicket() {
        // prepare
        val operationName = "FECompTotXRequest"
        val service = newService(operationName, "ok")

        // act
        val maxTicketsPerRequest = service.getMaxItemsPerTicket()

        // assert
        assert(maxTicketsPerRequest == 250)
        val request: ParameterTypeRequest = getRequest(service, operationName)
        assert(request.operationName == operationName)
    }

    @Test
    fun newTicketC_noDocumentRequired() {
        // prepare
        val operationName = "FECAESolicitar"
        val service = newService(operationName) {
            mockCall("FECompUltimoAutorizado") {
                loadResponse(SERVICE_NAME, "ok")
            }
            mockCall(operationName) {
                loadResponse(SERVICE_NAME, "ticketC_noDocumentRequired")
            }
        }

        // act
        val ticket: Ticket<TicketItem> = service.newTicketC(3) {
            addServiceItem(
                totalValue = 2000.0,
                startDate = DateTime.parse("2021-11-15T00:00:00"),
                endDate = DateTime.parse("2021-12-15T00:00:00"),
                paymentDueDate = DateTime.parse("2021-12-25T00:00:00")
            )
        }

        // assert
        assert(ticket.status == TicketStatus.ACCEPTED)
        assert(ticket.number == 71503980500640)
        assert(ticket.items[0].transactionType == TransactionType.Services)
        assert(ticket.items[0].document == Document.new(DocumentType.OTHER, 0))
        assert(ticket.items[0].ticketIdFrom == 26)
        assert(ticket.items[0].ticketIdTo == 26)
        assert(ticket.items[0].netValue == 2000.0)
        assert(ticket.items[0].totalValue == 2000.0)
        assert(ticket.items[0].serviceStartDate == DateTime.parse("2021-11-15T00:00:00"))
        assert(ticket.items[0].serviceEndDate == DateTime.parse("2021-12-15T00:00:00"))
        assert(ticket.items[0].paymentDueDate == DateTime.parse("2021-12-25T00:00:00"))
        assert(ticket.items[0].currencyType == CurrencyType.ARS)
    }

    private fun newService(
        operationName: String,
        responseName: String? = null,
        mockCalls: (TestSoapClient.() -> Unit)? = null
    ): TicketService = withClient(
        TicketService(
            localConfig = TicketServiceConfig.local(Environment.TEST),
            exportConfig = TicketServiceConfig.export(Environment.TEST),
            authenticationService = mock()
        )
    ) {
        mockCalls?.invoke(this) ?: mockCall(operationName) {
            loadResponse(SERVICE_NAME, responseName!!)
        }
    }
}
