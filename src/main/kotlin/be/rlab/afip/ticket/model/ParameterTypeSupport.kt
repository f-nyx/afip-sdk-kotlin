package be.rlab.afip.ticket.model

abstract class ParameterTypeSupport<T : ParameterType> {
    abstract fun all(): List<T>

    abstract fun new(
        id: String,
        description: String
    ): T

    fun resolve(
        id: String,
        description: String
    ): T {
        validate(id, description)
        return all().find { type -> type.id == id } ?: new(id, description)
    }

    private fun validate(
        id: String,
        description: String
    ) {
        val types = all().filter { it.id == id || it.description == description }
        require(types.isEmpty() || types.size == 1) {
            "Constraint violation: there are more than one element that contains the same id or description"
        }
    }
}
