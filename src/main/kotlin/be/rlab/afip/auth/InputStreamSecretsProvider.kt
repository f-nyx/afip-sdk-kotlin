package be.rlab.afip.auth

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.security.KeyStore

class InputStreamSecretsProvider(
    private val keyStore: InputStream,
    password: String,
    certificateAlias: String,
    privateKeyAlias: String = certificateAlias
) : SecretsProvider(password, certificateAlias, privateKeyAlias) {
    private val input: ByteArray = keyStore.use {
        val out = ByteArrayOutputStream()
        it.copyTo(out)
        out.toByteArray()
    }

    override fun loadKeyStore(): KeyStore {
        return KeyStore.getInstance(DEFAULT_KEYSTORE_TYPE).apply {
            load(ByteArrayInputStream(input), password.toCharArray())
        }
    }
}
