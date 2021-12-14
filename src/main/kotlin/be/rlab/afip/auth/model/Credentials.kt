package be.rlab.afip.auth.model

/** This class represents the credentials for a single service.
 *
 * AFIP credentials always apply to a single service. Once authenticated, the credentials are valid
 * for 12 hours.
 */
data class Credentials(
    /** Service these credentials are valid for. */
    val serviceName: String,
    /** Access token. */
    val token: String,
    /** Token signature. */
    val sign: String,
    /** CUIT number. */
    val cuit: Long,
    /** Identifies the token issuer. */
    val source: String,
    /** Identifies the token requester. */
    val destination: String,
    /** Timestamp, in millis, when the credentials were issued. */
    val issuedAt: Long,
    /** Timestamp, in millis, when the credentials expire. */
    val expiresAt: Long
)
