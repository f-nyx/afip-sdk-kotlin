package be.rlab.afip.person.model

data class Email(
    val address: String,
    val type: String,
    val status: EmailStatus
)
