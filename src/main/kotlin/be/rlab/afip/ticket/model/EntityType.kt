package be.rlab.afip.ticket.model

enum class EntityType(val description: String) {
    CORPORATE("Persona Jurídica"),
    INDIVIDUAL("Persona Física"),
    OTHER("Otro tipo de Entidad");

    companion object {
        fun of(description: String): EntityType = values().find { entityType ->
            entityType.description == description
        } ?: INDIVIDUAL
    }
}
