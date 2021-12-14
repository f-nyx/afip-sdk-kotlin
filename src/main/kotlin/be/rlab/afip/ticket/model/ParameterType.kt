package be.rlab.afip.ticket.model

/** Parameters are enumerations of some concept.
 */
open class ParameterType(
    /** Unique identifier assigned by AFIP. */
    val id: String,
    /** Parameter type description. */
    val description: String
) {
    override fun toString(): String {
        return if (this.javaClass.superclass == ParameterType::class.java) {
            description
        } else {
            this.javaClass.simpleName
        }
    }
}
