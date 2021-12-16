package be.rlab.afip.auth.request

import be.rlab.afip.auth.AuthServiceConfig
import be.rlab.afip.support.SoapRequest

class LoginRequest(
    internal val ticketPayload: String
) : SoapRequest() {
    override val operationName: String = "loginCms"
    override val serviceName: String = AuthServiceConfig.SERVICE_NAME

    override fun build(): String {
        return """
            <?xml version="1.0" encoding="utf-8"?>
            <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:wsaa="http://wsaa.view.sua.dvadac.desein.afip.gov">
               <soapenv:Header/>
               <soapenv:Body>
                  <wsaa:loginCms>
                     <wsaa:in0>$ticketPayload</wsaa:in0>
                  </wsaa:loginCms>
               </soapenv:Body>
            </soapenv:Envelope>
        """.trimIndent()
    }
}
