package be.rlab.afip.person.model

data class LegalFramework(
    val id: Long,
    val type: String?,
    val status: TaxPersonStatus,
    val description: String?,
    val taxId: Long,
    val period: String
)
