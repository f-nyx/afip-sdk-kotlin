package be.rlab.afip.person.model

enum class TaxIdStatus(val value: String) {
    ACTIVE("ACTIVO"),
    INACTIVE("INACTIVO");

    companion object {
        fun of(value: String): TaxIdStatus = entries.find { entityType ->
            entityType.value == value
        } ?: throw RuntimeException("invalid type: $value")
    }
}
