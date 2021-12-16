package be.rlab.afip.ticket

import be.rlab.afip.ticket.model.*
import be.rlab.afip.ticket.request.CreateExportTicketRequest
import be.rlab.afip.ticket.request.CreateTicketRequest
import org.joda.time.DateTime
import kotlin.math.abs
import kotlin.random.Random

class CreateTicketRequestBuilder(
    private val ticketService: TicketService,
    private val pointOfSaleId: Int,
    private val ticketType: TicketType
) {
    private data class ExportRecipient(
        val transactionType: TransactionType,
        val customerFullName: String,
        val customerFullAddress: String,
        val customerLegalId: String,
        val customerType: EntityType,
        val targetLocation: Location,
        val language: Language,
        val paymentMethod: String,
        val paymentDate: DateTime,
        val currencyType: CurrencyType,
        val issuedAt: DateTime
    )

    val items: MutableList<TicketItem> = mutableListOf()
    val exportItems: MutableList<ExportTicketItem> = mutableListOf()
    private var exportRecipient: ExportRecipient? = null

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
        require(!ticketType.export) { "For export tickets use addExportServiceItem()" }

        items += when(ticketType) {
            TicketType.TicketC -> TicketItem.ticketC(
                transactionType = TransactionType.Services,
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

    fun exportRecipient(
        customerFullName: String,
        customerFullAddress: String,
        customerLegalId: String,
        customerType: EntityType,
        targetLocation: Location,
        language: Language,
        paymentMethod: String,
        paymentDate: DateTime = DateTime.now(),
        transactionType: TransactionType = TransactionType.Services,
        currencyType: CurrencyType = CurrencyType.USD,
        issuedAt: DateTime = DateTime.now()
    ) {
        exportRecipient = ExportRecipient(
            transactionType, customerFullName, customerFullAddress, customerLegalId, customerType,
            targetLocation, language, paymentMethod, paymentDate, currencyType, issuedAt
        )
    }

    fun addExportServiceItem(
        code: String,
        description: String,
        totalValue: Double,
        discount: Double = 0.0
    ) {
        require(ticketType.export) { "For local tickets use addServiceItem()" }

        exportItems += when(ticketType) {
            TicketType.TicketE -> ExportTicketItem.ticketE(
                productCode = code,
                productDescription = description,
                quantity = 1,
                unit = UnitType.Units,
                unitValue = totalValue,
                discount = discount
            )
            else -> throw UnsupportedOperationException("This builder does not support tickets of type $ticketType yet")
        }
    }

    fun build(): CreateTicketRequest {
        require(items.isNotEmpty()) { "No items added to the ticket." }
        return CreateTicketRequest(pointOfSaleId, ticketType, items)
    }

    fun buildExport(): CreateExportTicketRequest {
        require(exportItems.isNotEmpty()) { "No items added to the ticket." }
        requireNotNull(exportRecipient) { "No recipient configured, look at exportRecipient()" }

        return exportRecipient?.let { recipient ->
            val locationEntity = ticketService.getLocationEntities().single { locationEntity ->
                locationEntity.location == recipient.targetLocation && locationEntity.type == recipient.customerType
            }
            val currency = ticketService.getCurrencyValues(recipient.paymentDate).single { currencyValue ->
                currencyValue.currency == recipient.currencyType
            }
            val ticketId = nextTicketId

            CreateExportTicketRequest(
                ticketType,
                pointOfSaleId,
                customIdentifier = abs(Random.nextInt()),
                ticketId = ticketId,
                issueDate = recipient.issuedAt,
                transactionType = recipient.transactionType,
                targetLocation = recipient.targetLocation,
                locationEntity = locationEntity,
                customerFullName = recipient.customerFullName,
                customerFullAddress = recipient.customerFullAddress,
                customerLegalId = recipient.customerLegalId,
                currency = currency,
                language = recipient.language,
                paymentMethod = recipient.paymentMethod,
                paymentDate = recipient.paymentDate,
                incoterms = null,
                exportItems
            )
        }!!
    }
}
