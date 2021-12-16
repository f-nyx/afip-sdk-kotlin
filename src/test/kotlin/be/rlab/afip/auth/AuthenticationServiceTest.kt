package be.rlab.afip.auth

import be.rlab.afip.auth.request.LoginRequest
import be.rlab.afip.config.Environment
import be.rlab.afip.support.ServiceTestSupport.getRequest
import be.rlab.afip.support.ServiceTestSupport.withClient
import be.rlab.afip.support.store.FileSystemObjectStore
import be.rlab.afip.ticket.TicketServiceConfig
import org.bouncycastle.cms.CMSSignedData
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder
import org.junit.jupiter.api.Test
import java.io.File
import java.security.cert.X509Certificate
import java.util.*

class AuthenticationServiceTest {
    companion object {
        private const val SERVICE_NAME = "auth"
        private const val CUIT: Long = 20304050603
        private val storeDir: File = File(System.getProperty("java.io.tmpdir"), "test-store")
    }

    init {
        storeDir.mkdirs()
    }

    private val store = FileSystemObjectStore(storeDir)
    private val secretsProvider: SecretsProvider = TestSecretsProvider(store).init()

    @Test
    fun authenticate() {
        // prepare
        val service = withClient(createAuthenticationService()) {
            mockCall("loginCms") {
                loadResponse(SERVICE_NAME, "ok")
            }
        }

        // act
        val credentials = service.authenticate(TicketServiceConfig.LOCAL_SERVICE_NAME)

        // assert
        assert(credentials.serviceName == TicketServiceConfig.LOCAL_SERVICE_NAME)
        assert(credentials.cuit == CUIT)
        assert(credentials.source == "CN=wsaahomo, O=AFIP, C=AR, SERIALNUMBER=CUIT 33693450239")
        assert(credentials.destination == "SERIALNUMBER=CUIT $CUIT, CN=NyxCo")

        val request: LoginRequest = getRequest(service, "loginCms")
        assert(request.operationName == "loginCms")
        val signedData = CMSSignedData(Base64.getDecoder().decode(request.ticketPayload))

        val validSignature = signedData.signerInfos.signers.all { signer ->
            signer.verify(
                JcaSimpleSignerInfoVerifierBuilder()
                    .setProvider("BC")
                    .build(secretsProvider.certificate() as X509Certificate)
            )
        }
        assert(validSignature)
    }

    private fun createAuthenticationService(): AuthenticationService {
        return AuthenticationService(
            config = AuthServiceConfig.new(Environment.TEST, cuit = CUIT),
            credentialsCache = CredentialsCache(store, enabled = false),
            secretsProvider = secretsProvider
        )
    }
}
