package be.rlab.afip.support

import be.rlab.afip.auth.AuthenticationService
import org.jsoup.nodes.Document

class TestSoapClient(
    serviceName: String,
    endpoint: String,
    authenticationService: AuthenticationService?
) : SoapClient(serviceName, endpoint, authenticationService) {
    companion object {
        fun from(client: SoapClient): TestSoapClient {
            return TestSoapClient(client.serviceName, client.endpoint, client.authenticationService)
        }
    }

    data class MockCall(val operationName: String?) {
        fun loadResponse(
            serviceName: String,
            responseName: String
        ): String {
            return SoapTestUtils.loadResponse(serviceName, operationName!!, responseName)
        }
    }

    private val calls: MutableMap<String, MockCall.() -> String> = mutableMapOf()
    private val requests: MutableMap<String, List<SoapRequest>> = mutableMapOf()

    @Suppress("UNCHECKED_CAST")
    fun<T : SoapRequest> getRequest(
        operationName: String,
        index: Int = 0
    ): T {
        require(requests.containsKey(operationName)) { "no requests recorded for operation $operationName" }
        return requests.getValue(operationName)[index] as T
    }

    fun mockCall(
        operationName: String,
        responseBuilder: MockCall.() -> String
    ) {
        calls[operationName] = responseBuilder
    }

    override fun <T> call(
        request: SoapRequest,
        failOnError: Boolean,
        responseBuilder: Document.() -> T
    ): T {
        require(calls.isNotEmpty()) { "there are no mocked calls" }
        val response = calls[request.operationName]
            ?: throw RuntimeException("operation ${request.operationName} not mocked")

        requests[request.operationName] = (requests[request.operationName] ?: emptyList()) + request

        return responseBuilder(
            createAndValidateResponse(response(MockCall(request.operationName)), failOnError)
        )
    }
}
