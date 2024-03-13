package be.rlab.afip.person.model

data class Category(
    val id: Long,
    val description: String?,
    val taxId: Long,
    val status: TaxPersonStatus,
    val period: String
)
