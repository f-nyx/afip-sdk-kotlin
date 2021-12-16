package be.rlab.afip.ticket.model

data class LocationEntity(
    val location: Location,
    val type: EntityType,
    val cuit: Long
)
