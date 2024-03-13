package be.rlab.afip.person.request

import be.rlab.afip.person.PersonServiceConfig
import be.rlab.afip.support.SoapRequest

class GetPersonRequest(private val personId: String) : SoapRequest() {
    override val operationName: String = "getPersona"
    override val serviceName: String = PersonServiceConfig.SERVICE_NAME

    override fun build(): String {
        val auth = credentials?.let { """
            <token>${credentials?.token}</token>
            <sign>${credentials?.sign}</sign>
            <cuitRepresentada>${credentials?.cuit}</cuitRepresentada>
        """.trimIndent() } ?: ""

        return """
            <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/" xmlns:a4="http://a4.soap.ws.server.puc.sr/">
               <soap:Header/>
               <soap:Body>
                  <a4:getPersona>
                     $auth
                     <idPersona>$personId</idPersona>
                  </a4:getPersona>
               </soap:Body>
            </soap:Envelope>
        """.trimIndent()
    }
}
