package be.rlab.afip.person.model

import org.joda.time.DateTime

data class Relationship(
    val type: String?,
    val subtype: String?,
    val cuit: Long,
    val associatedCuit: Long,
    val startDate: DateTime,
    val expirationDate: DateTime?
)
