package be.rlab.afip.person.model

enum class AddressType(val value: String) {
    FISCAL("FISCAL"),
    LEGAL("LEGAL/REAL"),
    BUSINESS("LOCALES Y ESTABLECIMIENTOS");

    companion object {
        fun of(value: String): AddressType = entries.find { entityType ->
            entityType.value == value
        } ?: FISCAL
    }
}
