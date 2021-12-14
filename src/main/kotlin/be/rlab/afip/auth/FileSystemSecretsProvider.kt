package be.rlab.afip.auth

import java.io.File
import java.security.KeyStore

class FileSystemSecretsProvider(
    private val keyStoreFile: File,
    password: String,
    certificateAlias: String,
    privateKeyAlias: String = certificateAlias
) : SecretsProvider(password, certificateAlias, privateKeyAlias) {
    init {
        require(keyStoreFile.exists()) { "The key store file does not exist." }
    }

    override fun loadKeyStore(): KeyStore {
        return keyStoreFile.inputStream().use { keyStoreStream ->
            KeyStore.getInstance(DEFAULT_KEYSTORE_TYPE).apply {
                load(keyStoreStream, password.toCharArray())
            }
        }
    }
}
