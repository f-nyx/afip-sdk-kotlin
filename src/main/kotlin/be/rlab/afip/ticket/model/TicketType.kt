package be.rlab.afip.ticket.model

/** Enumeration of well-know ticket types.
 */
open class TicketType(
    id: Int,
    description: String
) : ParameterType(id.toString(), description) {
    object TICKET_A : TicketType(1, "Factura A")
    object DEBIT_NOTE_A : TicketType(2, "Nota de Débito A")
    object CREDIT_NOTE_A : TicketType(3, "Nota de Crédito A")
    object TICKET_B : TicketType(6, "Factura B")
    object DEBIT_NOTE_B : TicketType(7, "Nota de Débito B")
    object CREDIT_NOTE_B : TicketType(8, "Nota de Crédito B")
    object RECEIPT_A : TicketType(4, "Recibos A")
    object RECEIPT_B : TicketType(9, "Recibos B")
    object TICKET_C : TicketType(11, "Factura C")
    object DEBIT_NOTE_C : TicketType(12, "Nota de Débito C")
    object CREDIT_NOTE_C : TicketType(13, "Nota de Crédito C")
    object RECEIPT_C : TicketType(15, "Recibo C")

    companion object : ParameterTypeSupport<TicketType>() {
        override fun all(): List<TicketType> = listOf(
            TICKET_A, TICKET_B, TICKET_C,
            DEBIT_NOTE_A, DEBIT_NOTE_B, DEBIT_NOTE_C,
            CREDIT_NOTE_A, CREDIT_NOTE_B, CREDIT_NOTE_C,
            RECEIPT_A, RECEIPT_B, RECEIPT_C
        )

        override fun new(id: String, description: String): TicketType =
            TicketType(id.toInt(), description)
    }
}
