package be.rlab.afip.ticket.request

import be.rlab.afip.support.SoapRequest

/** This request retrieves AFIP parameters like ticket types and transaction types.
 * These requests do not accept parameters, they only require the operation name.
 */
class ParameterTypeRequest(
    override val operationName: String
) : SoapRequest() {

    override fun build(): String {
        return """
            <soap:Envelope xmlns:soap="http://www.w3.org/2003/05/soap-envelope" xmlns:ar="http://ar.gov.afip.dif.FEV1/">
               <soap:Header/>
               <soap:Body>
                  <ar:$operationName>
                     $authHeader
                  </ar:$operationName>
               </soap:Body>
            </soap:Envelope>
        """.trimIndent()
    }
}
