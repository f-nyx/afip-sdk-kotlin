package be.rlab.afip.support

import be.rlab.afip.auth.AuthenticationService
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.parser.Parser
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

/** HTTP client to call AFIP SOAP services.
 *
 * SOAP services have a single endpoint for all supported operations. This client
 * abstracts the HTTP transport layer and the authentication for a single service.
 *
 * Authenticated clients will use the [AuthenticationService] to retrieve the credentials before
 * calling an operation. The [AuthenticationService] handles the credentials' cache, so there is
 * no need to manage the credentials outside this client.
 *
 * Unauthenticated clients don't need credentials to call an operation in the service.
 */
open class SoapClient(
    /** Name of the target service. */
    internal val serviceName: String,
    /** Endpoint of the service. */
    internal val endpoint: String,
    /** If this is an authenticated client, it contains the service to retrieve credentials. */
    internal val authenticationService: AuthenticationService?
) {
    companion object {
        private val logger: Logger = LoggerFactory.getLogger(SoapClient::class.java)

        /** Creates an authenticated client.
         * @param serviceName Name of the target service.
         * @param endpoint Endpoint of the service.
         * @param authenticationService Service to retrieve credentials.
         * @return a new client.
         */
        fun authenticated(
            serviceName: String,
            endpoint: String,
            authenticationService: AuthenticationService
        ): SoapClient = SoapClient(serviceName, endpoint, authenticationService)

        /** Creates an unauthenticated client.
         * @param serviceName Name of the target service.
         * @param endpoint Endpoint of the service.
         * @return a new client.
         */
        fun notAuthenticated(
            serviceName: String,
            endpoint: String
        ): SoapClient = SoapClient(serviceName, endpoint, authenticationService = null)
    }

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    /** Calls an operation and returns the raw response.
     * @param request SOAP request containing the data to execute the operation.
     * @return returns the response body.
     */
    fun call(request: SoapRequest): String {
        return call(request) { html() }
    }

    /** Calls an operation and builds the response.
     * @param request SOAP request containing the data to execute the operation.
     * @param failOnError If true, it throws an error if service errors are found.
     * @param responseBuilder Function to build the response.
     * @return returns the resolved response.
     */
    open fun<T> call(
        request: SoapRequest,
        failOnError: Boolean = true,
        responseBuilder: Document.() -> T
    ): T {
        authenticationService?.let {
            logger.debug("service requires authorization")
            request.authorize(authenticationService.authenticate(serviceName))
        }
        logger.debug("building SOAP payload")
        val payload = request.build()
        logger.debug("calling operation ${request.operationName} on endpoint $endpoint")
        val httpRequest = Request.Builder()
            .url(endpoint)
            .post(payload.toRequestBody("application/soap+xml".toMediaType()))
            .header("SOAPAction", "AFIP")
            .build()
        val response = httpClient.newCall(httpRequest).execute().use { response ->
            logger.debug("reading response payload")
            response.body?.string() ?: throw RuntimeException("invalid response body")
        }

        return responseBuilder(createAndValidateResponse(response, failOnError))
    }

    protected fun createAndValidateResponse(
        response: String,
        failOnError: Boolean
    ): Document {
        logger.debug("validating and building response")
        val document: Document = Jsoup.parse(Parser.unescapeEntities(response, true), Parser.xmlParser())
        return validate(document, failOnError)
    }

    private fun validate(
        document: Document,
        failOnError: Boolean
    ): Document {
        // Checks standard SOAP errors.
        val fault = document.nsSelect("Fault")
        if (fault.isNotEmpty()) {
            val errorCode = fault.select("faultCode")
            val errorMessage = fault.select("faultString")
            val details = fault.select("detail")
            throw RuntimeException("[${errorCode.text()}] ${errorMessage.text()} - ${details.text()}")
        }
        // Checks known service errors.
        val errors = document.serviceErrors()
        if (errors.isNotEmpty() && failOnError) {
            throw ServiceException(errors)
        }

        return document
    }
}
