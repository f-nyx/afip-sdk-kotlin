package be.rlab.afip.ticket.model

/** Enumeration of well-know units types.
 */
open class UnitType(
    id: String,
    description: String
) : ParameterType(id, description) {
    object Grams : UnitType("14", "gramos")
    object Liters : UnitType("5", "litros")
    object Units : UnitType("7", "unidades")
    object Meters : UnitType("2", "metros")
    object Other : UnitType("98", "otras unidades")

    companion object : ParameterTypeSupport<UnitType>() {
        override fun all(): List<UnitType> = listOf(
            Grams, Liters, Units, Meters, Other
        )

        override fun new(id: String, description: String): UnitType =
            UnitType(id, description)
    }
}
