package be.rlab.afip.person.model

enum class PhoneLineType(val value: String) {
    HOME("FIJO"),
    MOBILE("MÓVIL");

    companion object {
        fun of(value: String): PhoneLineType = entries.find { entityType ->
            entityType.value == value
        } ?: HOME
    }
}
