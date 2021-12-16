package be.rlab.afip.ticket.model

/** Enumeration of well-know transaction types.
 * Transaction types are the same for both local and export services.
 */
open class TransactionType(
    id: Int,
    description: String
) : ParameterType(id.toString(), description) {
    object Products : TransactionType(1, "Producto")
    object Services : TransactionType(2, "Servicios")
    object ProductsAndServices : TransactionType(3, "Productos y Servicios")

    companion object : ParameterTypeSupport<TransactionType>() {
        override fun all(): List<TransactionType> = listOf(Products, Services, ProductsAndServices)

        override fun new(id: String, description: String): TransactionType =
            TransactionType(id.toInt(), description)
    }
}
