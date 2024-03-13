package be.rlab.afip.person.model

enum class PersonType(val description: String) {
    CORPORATE("JURIDICA"),
    INDIVIDUAL("FISICA");

    companion object {
        fun of(description: String): PersonType = values().find { entityType ->
            entityType.description == description
        } ?: INDIVIDUAL
    }
}
