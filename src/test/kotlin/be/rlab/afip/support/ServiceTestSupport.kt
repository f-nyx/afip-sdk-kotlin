package be.rlab.afip.support

object ServiceTestSupport {
    fun <T : Any> withClient(
        service: T,
        mockClient: TestSoapClient.() -> Unit
    ): T {
        val property = service::class.java.getDeclaredField("client")
            ?: throw RuntimeException("The object doesn't have the field 'client' of type SoapClient")
        require(property.type == SoapClient::class.java) { "The field 'client' must be of type SoapClient" }
        property.isAccessible = true
        val client: TestSoapClient = TestSoapClient.from(property.get(service) as SoapClient)
        mockClient(client)
        property.set(service, client)
        return service
    }

    fun getClient(service: Any): TestSoapClient {
        val property = service::class.java.getDeclaredField("client")
            ?: throw RuntimeException("The object doesn't have the field 'client' of type SoapClient")
        require(property.type == SoapClient::class.java) { "The field 'client' must be of type SoapClient" }
        property.isAccessible = true
        return property.get(service) as TestSoapClient
    }

    @Suppress("UNCHECKED_CAST")
    fun<T : SoapRequest> getRequest(
        service: Any,
        operationName: String,
        index: Int = 0
    ): T {
        return getClient(service).getRequest(operationName, index) as T
    }
}
