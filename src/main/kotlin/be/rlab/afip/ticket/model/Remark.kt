package be.rlab.afip.ticket.model

/** A [Ticket] might have remarks when it's created.
 * This class represents a remark returned by the issue operation.
 */
data class Remark(
    val code: Int,
    val message: String
) {
    companion object {
        fun new(
            code: Int,
            message: String
        ): Remark = Remark(code, message)
    }
}
