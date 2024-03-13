package be.rlab.afip.person.model

enum class TaxPersonStatus(val value: String) {
    ACTIVE("ACTIVO"),
    ACTIVE_PENDING("ACTIVO EN TRAMITE"),
    DISABLED_TEMPORARY("BAJA PROVISORIA"),
    DISABLED_PERMANENT("BAJA DEFINITIVA"),
    EXEMPT("EXENTO"),
    EXEMPT_PENDING("EXENTO EN TRAMITE"),
    UNREGISTERED("NO APORTANTE/NO ALCANZADO");

    companion object {
        fun of(value: String): TaxPersonStatus = entries.find { entityType ->
            entityType.value == value
        } ?: ACTIVE
    }
}
