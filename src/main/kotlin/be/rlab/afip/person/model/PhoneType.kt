package be.rlab.afip.person.model

enum class PhoneType(val value: String) {
    PERSONAL("PARTICULAR"),
    COMMERCIAL("COMERCIAL"),
    ACCOUNTANT("DEL CONTADOR"),
    CONSULTANT("DEL ASESOR"),
    PERSONAL_OVER_INTERNET("PERSONAL INTERNET"),
    MARKETING("CAMPAÑA TELEFÓNICA"),
    CUSTOMS_CONTACT("CONTACTO ADUANERO");

    companion object {
        fun of(value: String): PhoneType = entries.find { entityType ->
            entityType.value == value
        } ?: PERSONAL
    }
}
