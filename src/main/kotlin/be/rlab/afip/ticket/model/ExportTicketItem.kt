package be.rlab.afip.ticket.model

/** Represents a single item in a ticket.
 */
data class ExportTicketItem(
    val productCode: String,
    val productDescription: String,
    val quantity: Int,
    val unit: UnitType,
    val unitValue: Double,
    val discount: Double,
    val totalValue: Double
) {
    companion object {
        fun ticketE(
            productCode: String,
            productDescription: String,
            quantity: Int,
            unit: UnitType,
            unitValue: Double,
            discount: Double
        ): ExportTicketItem {
            require(discount <= unitValue * quantity) { "The discount cannot be greater than the total value." }

            return ExportTicketItem(
                productCode, productDescription, quantity, unit, unitValue, discount,
                totalValue = (unitValue * quantity) - discount
            )
        }
    }
}
