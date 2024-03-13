package be.rlab.afip.person.model

data class Activity(
    val id: Long,
    val description: String?,
    val period: String,
    val nameCatalog: Long
)
