package be.rlab.afip

import be.rlab.afip.auth.AuthenticationService
import be.rlab.afip.auth.TestSecretsProvider
import be.rlab.afip.config.Environment
import be.rlab.afip.apps.AppsService
import be.rlab.afip.portal.PortalService
import be.rlab.afip.apps.certs.CertificateApp
import be.rlab.afip.support.store.FileSystemObjectStore
import be.rlab.afip.support.store.ObjectStore
import be.rlab.afip.ticket.TicketService
import org.junit.jupiter.api.Test
import java.io.File

class ServiceProviderTest {
    companion object {
        private val storeDir: File = File(System.getProperty("java.io.tmpdir"), "test-store")
    }
    private val store: ObjectStore

    init {
        storeDir.mkdirs()
        store = FileSystemObjectStore(storeDir)
        TestSecretsProvider(store).init()
    }

    @Test
    fun new() {
        val serviceProvider = ServiceProvider.new {
            environment { Environment.TEST }

            secretsProvider {
                cuit = 20304050603
                alias = "afiptest"
                password = "Test123"
                certificateManager { keyStoreName = "afip.testKeyStore" }
            }

            store {
                fileSystem {
                    storeDir = File("")
                }
            }

            portal {
                cuit = 20304050603
                password = "Test123"
            }
        }
        serviceProvider.getService<AuthenticationService>()
        serviceProvider.getService<TicketService>()
        serviceProvider.getService<PortalService>()
        serviceProvider.getService<AppsService>()
    }
}