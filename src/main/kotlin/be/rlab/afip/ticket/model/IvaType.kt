package be.rlab.afip.ticket.model

/** Enumeration of IVA (Impuesto al Valor Agregado) tax types.
 */
open class IvaType(
    id: String,
    description: String
) : ParameterType(id, description) {
    companion object : ParameterTypeSupport<IvaType>() {
        override fun all(): List<IvaType> = listOf()
        override fun new(id: String, description: String): IvaType = IvaType(id, description)
    }
}
