package be.rlab.afip.auth

import be.rlab.afip.auth.model.Credentials
import be.rlab.afip.auth.request.LoginRequest
import be.rlab.afip.auth.request.LoginTicketRequest
import be.rlab.afip.support.SoapClient
import be.rlab.afip.support.dateTimeMillis
import be.rlab.afip.support.string
import org.bouncycastle.cert.X509CertificateHolder
import org.bouncycastle.cert.jcajce.JcaCertStore
import org.bouncycastle.cms.CMSProcessableByteArray
import org.bouncycastle.cms.CMSSignedData
import org.bouncycastle.cms.CMSSignedDataGenerator
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.security.Security
import java.security.cert.X509Certificate
import java.util.*

/** This class authenticates AFIP services using the WSAA login service.
 *
 * Each individual service in the AFIP API requires authentication. This service issues [Credentials] for
 * a service. The [SoapClient] uses this service to provide transparent authentication to other services.
 *
 * @see https://www.afip.gob.ar/ws/WSAA/Especificacion_Tecnica_WSAA_1.2.2.pdf
 */
class AuthenticationService(
    /** Authentication service configuration. */
    private val config: AuthServiceConfig,
    private val credentialsCache: CredentialsCache,
    private val secretsProvider: SecretsProvider
) {
    companion object {
        const val BC_PROVIDER = "BC"
        private val logger: Logger = LoggerFactory.getLogger(AuthenticationService::class.java)
    }

    private val client: SoapClient = SoapClient.notAuthenticated(config.serviceName, config.endpoint)

    fun authenticate(serviceName: String): Credentials = credentialsCache.loadIfRequired(serviceName) {
        logger.debug("authentication started for service $serviceName")
        val encodedTicketPayload = createTicketPayload(serviceName)

        logger.debug("calling LoginCms operation")
        client.call(LoginRequest(encodedTicketPayload)) {
            logger.debug("login successful for service: $serviceName")
            Credentials(
                serviceName,
                token = string("token"),
                sign = string("sign"),
                cuit = config.cuit,
                source = string("source"),
                destination = string("destination"),
                issuedAt = dateTimeMillis("generationTime"),
                expiresAt = dateTimeMillis("expirationTime")
            )
        }
    }

    /** Creates the signed payload required by the authentication service.
     *
     * It uses the CMS (Cryptographic Message Syntax) format to sign and encapsulate the ticket payload.
     * The payload is signed with the X.509 certificate provided by AFIP.
     *
     * @param serviceName Service to authenticate.
     * @return the ticket payload as a CMS message encoded in base64.
     */
    private fun createTicketPayload(serviceName: String): String {
        if (Security.getProvider(BC_PROVIDER) == null) {
            logger.debug("initializing BouncyCastle security provider")
            Security.addProvider(BouncyCastleProvider())
        }

        logger.debug("retrieving private key and certificate from key store")
        val cert: X509Certificate = secretsProvider.certificate() as X509Certificate
        val generator = CMSSignedDataGenerator().apply {
            val sha1Signer = JcaContentSignerBuilder("SHA1withRSA")
                .setProvider(BC_PROVIDER)
                .build(secretsProvider.privateKey())

            addSignerInfoGenerator(
                JcaSignerInfoGeneratorBuilder(
                    JcaDigestCalculatorProviderBuilder()
                        .setProvider(BC_PROVIDER)
                        .build()
                ).build(sha1Signer, X509CertificateHolder(cert.encoded))
            )
            addCertificates(JcaCertStore(listOf(cert)))
        }

        logger.debug("generating signed authentication message")
        val payload = LoginTicketRequest(cert.subjectDN.toString(), config.dn, serviceName).build()
        val signedData: CMSSignedData = generator.generate(CMSProcessableByteArray(payload.toByteArray()), true)

        return Base64.getEncoder().encodeToString(signedData.encoded)
    }
}
