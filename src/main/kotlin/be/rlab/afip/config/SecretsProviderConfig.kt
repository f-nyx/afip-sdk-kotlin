package be.rlab.afip.config

import be.rlab.afip.auth.CertManagerSecretsProvider
import be.rlab.afip.auth.FileSystemSecretsProvider
import be.rlab.afip.auth.InputStreamSecretsProvider
import be.rlab.afip.auth.SecretsProvider
import be.rlab.afip.support.CertificateManager
import java.io.File
import java.io.InputStream

class SecretsProviderConfig(
    private val objectStoreConfig: StoreConfig
) {
    class FileSystemConfig {
        lateinit var keyStoreFile: File
    }
    class CertManagerConfig {
        lateinit var keyStoreName: String
    }
    class InputStreamConfig {
        lateinit var source: InputStream
    }

    private lateinit var lazySecretsProvider: () -> SecretsProvider
    private var secretsProvider: SecretsProvider? = null

    lateinit var password: String
    lateinit var alias: String
    var cuit: Long = 0

    fun fileSystem(callback: FileSystemConfig.() -> Unit): SecretsProviderConfig = apply {
        lazySecretsProvider = {
            val config = FileSystemConfig().apply(callback)
            FileSystemSecretsProvider(config.keyStoreFile, password, alias)
        }
    }

    fun inputStream(callback: InputStreamConfig.() -> Unit): SecretsProviderConfig = apply {
        lazySecretsProvider = {
            val config = InputStreamConfig().apply(callback)
            InputStreamSecretsProvider(config.source, password, alias)
        }
    }

    fun certificateManager(callback: CertManagerConfig.() -> Unit): SecretsProviderConfig = apply {
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
