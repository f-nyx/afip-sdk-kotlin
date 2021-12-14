package be.rlab.afip.auth

import be.rlab.afip.support.CertificateManager
import java.security.KeyStore

class CertManagerSecretsProvider(
    private val manager: CertificateManager,
    private val keyStoreName: String,
    password: String,
    certificateAlias: String,
    privateKeyAlias: String
) : SecretsProvider(password, certificateAlias, privateKeyAlias) {

    companion object {
        fun new(
            manager: CertificateManager,
            keyStoreName: String,
            alias: String,
            password: String
        ): CertManagerSecretsProvider = CertManagerSecretsProvider(
            manager,
            keyStoreName,
            password,
            certificateAlias = alias,
            privateKeyAlias = alias
        )
    }

    override fun loadKeyStore(): KeyStore {
        return manager.loadKeyStore(keyStoreName, password)
            ?: throw RuntimeException("Key store not found in the internal storage: $keyStoreName")
    }
}
