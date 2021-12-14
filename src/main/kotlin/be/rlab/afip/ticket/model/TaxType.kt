package be.rlab.afip.ticket.model

/** Enumeration of tax types.
 */
open class TaxType(
    id: String,
    description: String
) : ParameterType(id, description) {
    companion object : ParameterTypeSupport<TaxType>() {
        override fun all(): List<TaxType> = listOf()
        override fun new(id: String, description: String): TaxType = TaxType(id, description)
    }
}
