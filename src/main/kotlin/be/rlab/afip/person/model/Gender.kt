package be.rlab.afip.person.model

enum class Gender(val value: String) {
    MALE("MASCULINO"),
    FEMALE("FEMENINO"),
    OTHER("DESCONOCIDO");

    companion object {
        fun of(value: String): Gender = entries.find { entityType ->
            entityType.value == value
        } ?: throw RuntimeException("invalid type: $value")
    }
}
