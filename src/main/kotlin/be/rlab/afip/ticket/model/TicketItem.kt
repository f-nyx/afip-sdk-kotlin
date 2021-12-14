package be.rlab.afip.ticket.model

import be.rlab.afip.ticket.StaticParameters.maxValueWithoutIdentityC
import org.joda.time.DateTime

/** Represents a single item in a ticket.
 */
data class TicketItem(
    /** Transaction type this ticket is created for. */
    val transactionType: TransactionType,
    /** Document of the person that receives the ticket. */
    val document: Document,
    /** Id of the next ticket to generate. */
    val ticketIdFrom: Int,
    /** Some types of tickets supports range of ids. I don't understand the use case yet,
     * for tickets B and C this field is always equals to ticketIdFrom.
     */
    val ticketIdTo: Int,
    /** Date and time when this ticket was issued.
     *
     * For products, it can be 5 days before or after the operation.
     * It can't be greater than one month after the operation.
     *
     * For services, it can 10 days before or after the operation.
     */
    val issuedAt: DateTime,
    /** Net amount without taxes. */
    val netValueWithoutTaxes: Double,
    /** Net amount with taxes. */
    val netValue: Double,
    /** Exempt amount. */
    val exemptValue: Double,
    /** Sum of all IVA taxes. */
    val totalIva: Double,
    /** Sum of all other taxes. */
    val totalTaxes: Double,
    /** If the transaction type involves services, it must be the start date of the period this ticket belongs to. */
    val serviceStartDate: DateTime?,
    /** If the transaction type involves services, it must be the end date of the period this ticket belongs to. */
    val serviceEndDate: DateTime?,
    /** If the transaction type involves services, this is the due date to pay for the service. */
    val paymentDueDate: DateTime?,
    /** Currency type of all currency values. */
    val currencyType: CurrencyType
) {
    /** Ticket total amount */
    val totalValue: Double get() = netValueWithoutTaxes + netValue + exemptValue + totalIva + totalTaxes

    companion object {
        /** Creates an item for [TicketType.TICKET_C] tickets.
         * @param transactionType Transaction type.
         * @param ticketId Id of the ticket to create for this item.
         * @param issuedAt Date and time the operation occurred.
         * @param totalValue Total amount for this item.
         * @param document Target person document, required if totalValue is greater than [maxValueWithoutIdentityC].
         * @param serviceStartDate Start date of the service, required if the transaction type involves a service.
         * @param serviceStartDate End date of the service, required if the transaction type involves a service.
         * @param paymentDueDate Due date to pay a service, required if the transaction type involves a service.
         * @param currencyType Currency type of all the amounts.
         */
        fun ticketC(
            transactionType: TransactionType,
            ticketId: Int,
            totalValue: Double,
            issuedAt: DateTime = DateTime.now(),
            document: Document? = null,
            serviceStartDate: DateTime? = null,
            serviceEndDate: DateTime? = null,
            paymentDueDate: DateTime? = null,
            currencyType: CurrencyType = CurrencyType.ARS
        ): TicketItem {
            val isService = transactionType == TransactionType.SERVICES
                || transactionType == TransactionType.PRODUCTS_AND_SERVICES
            val validServiceFields: Boolean = serviceStartDate != null
                && serviceEndDate != null
                && paymentDueDate != null
            require(!isService || (isService && validServiceFields)) {
                "serviceStartDate, serviceEndDate and paymentDueDate are required for services"
            }
            require(totalValue <= maxValueWithoutIdentityC || document != null) {
                "Document is required for tickets greater than $maxValueWithoutIdentityC ARS"
            }
            return TicketItem(
                transactionType = transactionType,
                document = document ?: Document.new(DocumentType.OTHER, 0),
                ticketIdFrom = ticketId,
                ticketIdTo = ticketId,
                issuedAt = issuedAt,
                netValueWithoutTaxes = 0.0,
                netValue = totalValue,
                exemptValue = 0.0,
                totalIva = 0.0,
                totalTaxes = 0.0,
                serviceStartDate = serviceStartDate,
                serviceEndDate = serviceEndDate,
                paymentDueDate = paymentDueDate,
                currencyType = currencyType
            )
        }
    }
}
