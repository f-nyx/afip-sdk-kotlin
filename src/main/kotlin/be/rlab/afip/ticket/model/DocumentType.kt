package be.rlab.afip.ticket.model

/** Enumeration of well-know document types.
 */
open class DocumentType(
    id: Int,
    description: String
) : ParameterType(id.toString(), description) {
    object CUIT : DocumentType(80, "CUIT")
    object CUIL : DocumentType(86, "CUIL")
    object CDI : DocumentType(87, "CDI")
    object LE : DocumentType(89, "LE")
    object LC : DocumentType(90, "LC")
    object CIE : DocumentType(91, "CI Extranjera")
    object DNI : DocumentType(96, "DNI")
    object PASSPORT : DocumentType(94, "Pasaporte")
    object OTHER : DocumentType(99, "Doc. (Otro)")

    companion object : ParameterTypeSupport<DocumentType>() {
        override fun all(): List<DocumentType> = listOf(
            CUIT, CUIL, CDI, LE, LC, CIE, DNI, PASSPORT, OTHER
        )

        override fun new(id: String, description: String): DocumentType =
            DocumentType(id.toInt(), description)
    }
}
