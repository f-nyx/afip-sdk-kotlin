package be.rlab.afip.support

import java.lang.RuntimeException

class ServiceException(
    val errors: List<ServiceError>
) : RuntimeException() {
    override val message: String get() =
        errors.joinToString(" | ") { error ->
            "[${error.code}] ${error.message}"
        }
}
