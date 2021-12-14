package be.rlab.afip.ticket.model

import be.rlab.afip.support.ServiceError

/** Represents an issued ticket.
 */
data class Ticket(
    /** Ticket number, available only if the status is [TicketStatus.ACCEPTED]. */
    val number: Long?,
    val status: TicketStatus,
    val items: List<TicketItem>,
    val remarks: List<Remark>,
    val errors: List<ServiceError>
) {
    companion object {
        fun accepted(
            number: Long,
            items: List<TicketItem>,
            remarks: List<Remark>,
            errors: List<ServiceError>
        ): Ticket = Ticket(
            number = number,
            status = TicketStatus.ACCEPTED,
            items = items,
            remarks = remarks,
            errors = errors
        )

        fun rejected(
            items: List<TicketItem>,
            remarks: List<Remark>,
            errors: List<ServiceError>
        ): Ticket = Ticket(
            number = null,
            status = TicketStatus.REJECTED,
            items = items,
            remarks = remarks,
            errors = errors
        )
    }
}
