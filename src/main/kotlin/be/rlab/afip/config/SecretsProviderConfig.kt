package be.rlab.afip.config

import be.rlab.afip.auth.CertManagerSecretsProvider
import be.rlab.afip.auth.FileSystemSecretsProvider
import be.rlab.afip.auth.SecretsProvider
import be.rlab.afip.support.CertificateManager
import java.io.File

class SecretsProviderConfig(
    private val objectStoreConfig: StoreConfig
) {
    class FileSystemConfig {
        lateinit var keyStoreFile: File
    }
    class CertManagerConfig {
        lateinit var keyStoreName: String
    }

    private lateinit var lazySecretsProvider: () -> SecretsProvider
    private var secretsProvider: SecretsProvider? = null

    lateinit var password: String
    lateinit var alias: String
    var cuit: Long = 0

    fun fileSystem(callback: FileSystemConfig.() -> Unit) {
        lazySecretsProvider = {
            val config = FileSystemConfig().apply(callback)
            FileSystemSecretsProvider(config.keyStoreFile, password, alias)
        }
    }

    fun certificateManager(callback: CertManagerConfig.() -> Unit) {
        lazySecretsProvider = {
            val config = CertManagerConfig().apply(callback)
            CertManagerSecretsProvider.new(
                manager = CertificateManager(objectStoreConfig.build()),
                keyStoreName = config.keyStoreName,
                alias,
                password
            )
        }
    }

    fun build(): SecretsProvider {
        if (secretsProvider == null) {
            secretsProvider = lazySecretsProvider()
        }
        return secretsProvider!!
    }
}
