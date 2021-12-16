package be.rlab.afip.ticket.model

/** Enumeration of well-know countries and territories.
 */
open class Location(
    id: String,
    description: String
) : ParameterType(id, description) {
    object UnitedStates : Location("212", "ESTADOS UNIDOS")
    object Argentina : Location("200", "ARGENTINA")
    object Uruguay : Location("225", "URUGUAY")
    object Chile : Location("208", "CHILE")
    object Paraguay : Location("221", "PARAGUAY")
    object Bolivia : Location("202", "BOLIVIA")
    object Peru : Location("222", "PERU")
    object Venezuela : Location("226", "VENEZUELA")
    object Colombia : Location("205", "COLOMBIA")
    object Ecuador : Location("210", "ECUADOR")

    companion object : ParameterTypeSupport<Location>() {
        override fun all(): List<Location> = listOf(
            UnitedStates, Argentina, Uruguay, Chile, Paraguay, Bolivia, Peru, Venezuela, Colombia, Ecuador
        )

        override fun new(id: String, description: String): Location =
            Location(id, description)
    }
}
