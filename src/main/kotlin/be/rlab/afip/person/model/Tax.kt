package be.rlab.afip.person.model

import org.joda.time.DateTime

data class Tax(
    val id: Long,
    val description: String?,
    val status: TaxPersonStatus,
    val period: String,
    val registrationDate: DateTime,
    val registrationDay: Int
)
