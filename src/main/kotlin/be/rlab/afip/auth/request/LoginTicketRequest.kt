package be.rlab.afip.auth.request

import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat

class LoginTicketRequest(
    private val sourceDN: String,
    private val destinationDN: String,
    private val serviceName: String
) {
    private val formatter = ISODateTimeFormat.dateTime()

    fun build(): String {
        val uniqueId: String = (System.currentTimeMillis() / 1000).toString()
        val generationTime: DateTime = DateTime.now().minusHours(1)
        val expirationTime: DateTime = generationTime.plusDays(1).minusMinutes(1)

        return """
            <?xml version="1.0" encoding="utf-8"?>
            <loginTicketRequest version="1.0">
              <header>
                <source>$sourceDN</source>
                <destination>$destinationDN</destination>
                <uniqueId>$uniqueId</uniqueId>
                <generationTime>${formatter.print(generationTime)}</generationTime>
                <expirationTime>${formatter.print(expirationTime)}</expirationTime>
              </header>
              <service>${serviceName}</service>
            </loginTicketRequest>
        """.trimIndent()
    }
}
