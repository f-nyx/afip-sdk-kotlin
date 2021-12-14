package be.rlab.afip.ticket.model

/** Represents a legal ID.
 */
data class Document(
    /** Legal id type. */
    val type: DocumentType,
    /** Legal id number. */
    val number: Long
) {
    companion object {
        fun new(
            type: DocumentType,
            number: Long
        ): Document = Document(type, number)
    }
}
