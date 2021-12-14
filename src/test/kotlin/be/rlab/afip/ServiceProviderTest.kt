package be.rlab.afip

import be.rlab.afip.auth.AuthenticationService
import be.rlab.afip.auth.TestSecretsProvider
import be.rlab.afip.config.Environment
import be.rlab.afip.support.store.FileSystemObjectStore
import be.rlab.afip.ticket.TicketService
import org.junit.jupiter.api.Test
import java.io.File

class ServiceProviderTest {
    companion object {
        private val storeDir: File = File(System.getProperty("java.io.tmpdir"), "test-store")
    }

    init {
        storeDir.mkdirs()
        val store = FileSystemObjectStore(storeDir)
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
        }
        serviceProvider.getService<AuthenticationService>()
        serviceProvider.getService<TicketService>()
    }
}