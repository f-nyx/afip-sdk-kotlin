package be.rlab.afip.ticket.model

import org.joda.time.DateTime

/** Contains a currency value at a specific date.
 * This value is provided by AFIP only for reference, it's not accurate.
 */
data class CurrencyValue(
    val currency: CurrencyType,
    val value: Double,
    val date: DateTime
)
