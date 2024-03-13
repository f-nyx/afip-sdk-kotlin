package be.rlab.afip.person.model

enum class EmailStatus(val value: String) {
    CONFIRMED("Confirmado"),
    UNCONFIRMED("No confirmado"),
    UNVERIFIED("No Verificado");

    companion object {
        fun of(value: String): EmailStatus = entries.find { entityType ->
            entityType.value == value
        } ?: UNCONFIRMED
    }
}
