package be.rlab.afip.ticket.model

import be.rlab.afip.support.ServiceError

/** Represents an issued ticket.
 */
data class Ticket<T>(
    /** Ticket number, available only if the status is [TicketStatus.ACCEPTED]. */
    val number: Long?,
    val status: TicketStatus,
    val items: List<T>,
    val remarks: List<Remark>,
    val errors: List<ServiceError>
) {
    companion object {
        fun<T> accepted(
            number: Long,
            items: List<T>,
            remarks: List<Remark>,
            errors: List<ServiceError>
        ): Ticket<T> = Ticket(
            number = number,
            status = TicketStatus.ACCEPTED,
            items = items,
            remarks = remarks,
            errors = errors
        )

        fun<T> rejected(
            items: List<T>,
            remarks: List<Remark>,
            errors: List<ServiceError>
        ): Ticket<T> = Ticket(
            number = null,
            status = TicketStatus.REJECTED,
            items = items,
            remarks = remarks,
            errors = errors
        )
    }
}
