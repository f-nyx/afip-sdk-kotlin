package be.rlab.afip.ticket.model

/** Enumeration of well-know transaction types.
 */
open class TransactionType(
    id: Int,
    description: String
) : ParameterType(id.toString(), description) {
    object PRODUCTS : TransactionType(1, "Producto")
    object SERVICES : TransactionType(2, "Servicios")
    object PRODUCTS_AND_SERVICES : TransactionType(3, "Productos y Servicios")

    companion object : ParameterTypeSupport<TransactionType>() {
        override fun all(): List<TransactionType> = listOf(PRODUCTS, SERVICES, PRODUCTS_AND_SERVICES)

        override fun new(id: String, description: String): TransactionType =
            TransactionType(id.toInt(), description)
    }
}
