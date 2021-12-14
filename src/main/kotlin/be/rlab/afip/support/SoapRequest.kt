package be.rlab.afip.support

import be.rlab.afip.auth.model.Credentials

/** Must be extended by objects to build a SOAP request.
 *
 * It supports authorization. If provided, the implementation can use the [authHeader] field to
 * place the header where it's required.
 *
 * In order to make the authorization work, the [authorize] method must be invoked before
 * building the request.
 */
abstract class SoapRequest {
    private var credentials: Credentials? = null
    abstract val operationName: String

    protected val authHeader: String get() = credentials?.let {
        """
         <ar:Auth>
            <ar:Token>${credentials?.token}</ar:Token>
            <ar:Sign>${credentials?.sign}</ar:Sign>
            <ar:Cuit>${credentials?.cuit}</ar:Cuit>
         </ar:Auth>
        """.trimIndent()
    } ?: ""

    /** Builds the HTTP request.
     * @return the raw HTTP payload.
     */
    abstract fun build(): String

    /** Adds authorization information to this request.
     * @param credentials Credentials to authorize this request.
     * @return this request.
     */
    fun authorize(credentials: Credentials): SoapRequest = apply {
        this.credentials = credentials
    }
}
