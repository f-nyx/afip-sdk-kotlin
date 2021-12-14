package be.rlab.afip.ticket

import be.rlab.afip.ticket.model.*
import be.rlab.afip.ticket.request.CreateTicketRequest
import org.joda.time.DateTime

class CreateTicketRequestBuilder(
    private val ticketService: TicketService,
    private val pointOfSaleId: Int,
    private val ticketType: TicketType
) {
    val items: MutableList<TicketItem> = mutableListOf()
    private val nextTicketId: Int by lazy {
        ticketService.getLastTicketId(pointOfSaleId, ticketType) + 1
    }

    fun addServiceItem(
        totalValue: Double,
        startDate: DateTime,
        endDate: DateTime,
        paymentDueDate: DateTime,
        document: Document? = null,
        issuedAt: DateTime = DateTime.now(),
        currencyType: CurrencyType = CurrencyType.ARS
    ) {
        items += when(ticketType) {
            TicketType.TICKET_C -> TicketItem.ticketC(
                transactionType = TransactionType.SERVICES,
                ticketId = nextTicketId,
                totalValue,
                issuedAt,
                document,
                startDate,
                endDate,
                paymentDueDate,
                currencyType
            )
            else -> throw UnsupportedOperationException("This builder does not support tickets of type $ticketType yet")
        }
    }

    fun build(): CreateTicketRequest {
        require(items.isNotEmpty()) { "No items added to the ticket." }
        return CreateTicketRequest(pointOfSaleId, ticketType, items)
    }
}
