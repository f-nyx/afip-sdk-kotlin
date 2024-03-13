package be.rlab.afip.person

import be.rlab.afip.ServiceProvider
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import java.io.File
import kotlin.test.assertNotNull

@EnabledIfEnvironmentVariable(named = "AFIP_RUN_INTEGRATION", matches = "yes")
class PersonServiceIntegrationTest {
    companion object {
        val STORE_DIR: String by lazy { System.getenv("AFIP_TEST_STORE_DIR") }
        val KEY_STORE_FILE: String by lazy { System.getenv("AFIP_TEST_KEY_STORE_FILE") }
        val CUIT: Long by lazy { System.getenv("AFIP_TEST_CUIT").toLong() }
        val ALIAS: String by lazy { System.getenv("AFIP_TEST_ALIAS") }
        val PASSWORD: String by lazy { System.getenv("AFIP_TEST_PASSWORD") }
    }

    private val serviceProvider: ServiceProvider by lazy {
        ServiceProvider.new {
            store {
                fileSystem { storeDir = File(STORE_DIR) }
            }
            secretsProvider {
                cuit = CUIT
                alias = ALIAS
                password = PASSWORD

                fileSystem { keyStoreFile = File(KEY_STORE_FILE) }
            }
        }
    }

    @Test
    fun getPersonA4() {
        // prepare
        val service: PersonService = serviceProvider.getService()
        // act
        val person = service.getPersonA4("20002307554")
        // assert
        assertNotNull(person)
    }
}
