package be.rlab.afip.auth

import be.rlab.afip.support.CertificateManager
import be.rlab.afip.support.store.ObjectStore
import org.joda.time.DateTime
import java.security.KeyStore

class TestSecretsProvider(
    private val store: ObjectStore
) : SecretsProvider(PASSWORD, ALIAS) {
    companion object {
        private const val KEY_STORE_NAME = "afip.testKeyStore"
        private const val ALIAS = "afip.testSecrets"
        private const val PASSWORD = "Test123"
    }

    private val manager: CertificateManager = CertificateManager(store)

    fun init(): TestSecretsProvider = apply {
        if (!store.exists(KEY_STORE_NAME)) {
            createKeyStore()
        }
    }

    override fun loadKeyStore(): KeyStore {
        return manager.loadKeyStore(KEY_STORE_NAME, password)
            ?: throw RuntimeException("TeskKey store not found in the internal storage")
    }

    private fun createKeyStore() {
        val keyPair = manager.createKeyPair()
        val rootCa = manager.createRootCertificate(keyPair)
        val csr = manager.createCertRequest(keyPair, "NyxCo", "NyxFE", 20304050603)
        val cert = manager.createCert(rootCa, csr, keyPair, DateTime.now(), DateTime.now().plusYears(20))

        manager.saveKeyStore(KEY_STORE_NAME, ALIAS, PASSWORD, keyPair, cert)
    }
}