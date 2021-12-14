package be.rlab.afip.ticket.model

/** Ticket statuses.
 */
enum class TicketStatus(val value: String) {
    /** The ticket was rejected, check errors and remarks for further information. */
    REJECTED("R"),
    /** The ticket was issued successfully. */
    ACCEPTED("A");

    companion object {
        fun of(value: String): TicketStatus = values().find {
            it.value == value
        } ?: throw RuntimeException("Status not found: $value")
    }
}
