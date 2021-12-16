package be.rlab.afip.ticket.model

/** Enumeration of well-know currencies.
 * Currency types are the same for both local and export services.
 */
open class CurrencyType(
    id: String,
    description: String
) : ParameterType(id, description) {
    object ARS : CurrencyType("PES", "Pesos Argentinos")
    object USD : CurrencyType("DOL", "Dólar Estadounidense")
    object USDL : CurrencyType("002", "Dólar Libre EEUU")
    object EUR : CurrencyType("060", "Euro")

    companion object : ParameterTypeSupport<CurrencyType>() {
        override fun all(): List<CurrencyType> = listOf(
            ARS, USD, EUR, USDL
        )

        override fun new(id: String, description: String): CurrencyType =
            CurrencyType(id, description)
    }
}
