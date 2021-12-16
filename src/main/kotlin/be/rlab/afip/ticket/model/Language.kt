package be.rlab.afip.ticket.model

/** Enumeration of all supported ticket languages.
 */
open class Language(
    id: String,
    description: String
) : ParameterType(id, description) {
    object Spanish : Language("1", "Español")
    object English : Language("2", "Inglés")
    object Portuguese : Language("3", "Portugués")

    companion object : ParameterTypeSupport<Language>() {
        override fun all(): List<Language> = listOf(
            Spanish, English, Portuguese
        )

        override fun new(id: String, description: String): Language =
            Language(id, description)
    }
}
