package be.rlab.afip.auth

import java.security.KeyStore
import java.security.PrivateKey
import java.security.cert.Certificate
import java.security.cert.X509Certificate

/** Must be extended to provide access to the key store that contains the X.509 certificate
 * issued by AFIP and the private key used to generate the certificate.
 */
abstract class SecretsProvider(
    /** Password to open the key store. */
    val password: String,
    /** Alias of the certificate within the key store.
     */
    private val certificateAlias: String,
    /** Alias of the private key within the key store.
     * By default, private key and certificate are expected to have the same alias.
     */
    private val privateKeyAlias: String = certificateAlias
) {
    companion object {
        const val DEFAULT_KEYSTORE_TYPE = "pkcs12"
    }

    /** Opens and retrieves the key store.
     * @return a valid key store.
     */
    abstract fun loadKeyStore(): KeyStore

    /** Returns the private key from the key store.
     * @return a valid private key.
     */
    fun privateKey(): PrivateKey {
        return loadKeyStore().getKey(
            privateKeyAlias,
            password.toCharArray()
        ) as PrivateKey
    }

    /** Returns the certificate issued by AFIP.
     * @return a valid certificate.
     */
    fun certificate(): Certificate {
        return loadKeyStore().getCertificate(certificateAlias) as X509Certificate
    }
}
