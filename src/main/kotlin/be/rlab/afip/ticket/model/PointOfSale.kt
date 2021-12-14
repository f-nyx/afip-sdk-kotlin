package be.rlab.afip.ticket.model

/** Represents a Point of Sale registered in AFIP.
 */
data class PointOfSale(
    /** Unique identifier, required for serveral operations. */
    val id: Int,
    /** Type of tickets this point of sale can issue. It might be CAE or CAEA. */
    val ticketingMode: String,
    /** Indicates whether this */
    val blocked: Boolean
) {
    companion object {
        fun new(
            id: Int,
            ticketingMode: String,
            blocked: Boolean
        ): PointOfSale = PointOfSale(id, ticketingMode, blocked)
    }
}
