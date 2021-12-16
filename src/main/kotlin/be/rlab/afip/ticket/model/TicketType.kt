package be.rlab.afip.ticket.model

/** Enumeration of well-know ticket types.
 *
 * So far there is no collision between local and export services results. It assumes that both services
 * will always return a different set of ticket types, but be careful.
 */
open class TicketType(
    id: Int,
    description: String,
    /** Indicates whether this ticket type applies to export tickets. */
    val export: Boolean = false
) : ParameterType(id.toString(), description) {
    object TicketA : TicketType(1, "Factura A")
    object DebitNoteA : TicketType(2, "Nota de Débito A")
    object CreditNoteA : TicketType(3, "Nota de Crédito A")
    object TicketB : TicketType(6, "Factura B")
    object DebitNoteB : TicketType(7, "Nota de Débito B")
    object CreditNoteB : TicketType(8, "Nota de Crédito B")
    object ReceiptA : TicketType(4, "Recibos A")
    object ReceiptB : TicketType(9, "Recibos B")
    object TicketC : TicketType(11, "Factura C")
    object DebitNoteC : TicketType(12, "Nota de Débito C")
    object CreditNoteC : TicketType(13, "Nota de Crédito C")
    object ReceiptC : TicketType(15, "Recibo C")
    object TicketE : TicketType(19, "Facturas de Exportación", export = true)
    object DebitNoteE : TicketType(20, "Nota de Débito por Operaciones con el Exterior", export = true)
    object CreditNoteE : TicketType(21, "Nota de Crédito por Operaciones con el Exterior", export = true)
    object ReceiptE : TicketType(88, "Remito Electrónico", export = true)
    object DataSummary : TicketType(89, "Resumen de Datos", export = true)

    companion object : ParameterTypeSupport<TicketType>() {
        override fun all(): List<TicketType> = listOf(
            TicketA, TicketB, TicketC, TicketE,
            DebitNoteA, DebitNoteB, DebitNoteC, DebitNoteE,
            CreditNoteA, CreditNoteB, CreditNoteC, CreditNoteE,
            ReceiptA, ReceiptB, ReceiptC, ReceiptE, DataSummary
        )

        override fun new(id: String, description: String): TicketType =
            TicketType(id.toInt(), description)
    }
}
