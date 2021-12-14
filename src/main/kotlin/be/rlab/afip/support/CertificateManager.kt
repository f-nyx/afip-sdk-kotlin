package be.rlab.afip.support

import be.rlab.afip.auth.SecretsProvider
import be.rlab.afip.support.store.ObjectStore
import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x509.*
import org.bouncycastle.cert.X509CertificateHolder
import org.bouncycastle.cert.X509v3CertificateBuilder
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.openssl.PEMParser
import org.bouncycastle.operator.ContentSigner
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import org.bouncycastle.pkcs.PKCS10CertificationRequest
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder
import org.bouncycastle.util.io.pem.PemObject
import org.joda.time.DateTime
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.math.BigInteger
import java.nio.charset.Charset
import java.security.*
import java.security.cert.Certificate
import java.security.cert.X509Certificate
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.*
import javax.security.auth.x500.X500Principal

class CertificateManager(
    private val store: ObjectStore
) {
    companion object {
        const val BC_PROVIDER: String = "BC"
        const val SIGNATURE_ALGORITHM: String = "SHA256withRSA";
        const val KEY_ALGORITHM: String = "RSA";
        const val STORE_TYPE: String = "PKCS12";
    }

    init {
        if (Security.getProvider(BC_PROVIDER) == null) {
            Security.addProvider(BouncyCastleProvider())
        }
    }

    /** Creates an RSA key pair.
     * @param keyLength Key length, default is 4096 bytes.
     * @return the new key pair.
     */
    fun createKeyPair(keyLength: Int = 4096): KeyPair {
        val keyPairGenerator = KeyPairGenerator.getInstance(KEY_ALGORITHM, BC_PROVIDER).apply {
            initialize(keyLength)
        }
        return keyPairGenerator.generateKeyPair()
    }

    /** Creates a root Certificate Authority (CA) required to issue new certificates.
     * @param rootKeyPair Key pair to sign the root CA.
     * @return the new root CA.
     */
    fun createRootCertificate(rootKeyPair: KeyPair): X509Certificate {
        val rootSerialNum = BigInteger(SecureRandom().nextLong().toString())
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DATE, -1)
        val startDate: Date = calendar.time

        calendar.add(Calendar.YEAR, 1)
        val endDate: Date = calendar.time

        // Issued By and Issued To same for root certificate
        val rootCertIssuer = X500Name("CN=root-cert")
        val rootCertSubject: X500Name = rootCertIssuer
        val rootCertContentSigner = JcaContentSignerBuilder(SIGNATURE_ALGORITHM)
            .setProvider(BC_PROVIDER)
            .build(rootKeyPair.private)
        val rootCertBuilder: X509v3CertificateBuilder = JcaX509v3CertificateBuilder(
            rootCertIssuer, rootSerialNum, startDate, endDate, rootCertSubject, rootKeyPair.public
        )

        // Add Extensions
        // A BasicConstraint to mark root certificate as CA certificate
        val rootCertExtUtils = JcaX509ExtensionUtils()
        rootCertBuilder.addExtension(Extension.basicConstraints, true, BasicConstraints(true))
        rootCertBuilder.addExtension(
            Extension.subjectKeyIdentifier, false,
            rootCertExtUtils.createSubjectKeyIdentifier(rootKeyPair.public)
        )

        // Create a cert holder and export to X509Certificate
        val rootCertHolder = rootCertBuilder.build(rootCertContentSigner)
        return JcaX509CertificateConverter().setProvider(BC_PROVIDER).getCertificate(rootCertHolder)
    }

    /** Creates a certificate request (CSR) to allow a Certificate Authority (CA) issue a new certificate.
     * @param keyPair Key pair used to sign the CSR.
     * @param companyName Name of your company.
     * @param applicationName Name of the application that will use the certificate.
     * @param cuit CUIT of the person that owns the certificate, required by the AFIP CA.
     * @return the new Certificate Request.
     */
    fun createCertRequest(
        keyPair: KeyPair,
        companyName: String,
        applicationName: String,
        cuit: Long
    ): PKCS10CertificationRequest {
        val p10Builder: PKCS10CertificationRequestBuilder = JcaPKCS10CertificationRequestBuilder(
            X500Principal("C=AR,O=$companyName,CN=$applicationName,serialNumber=CUIT $cuit"),
            keyPair.public
        )

        val csrBuilder = JcaContentSignerBuilder(SIGNATURE_ALGORITHM)
        val signer: ContentSigner = csrBuilder.build(keyPair.private)

        return p10Builder.build(signer)
    }

    /** Issues a new certificate using the specified CA.
     * @param root CA that verifies the certificate.
     * @param csr Certificate request.
     * @param startDate Certificate will be valid from this date.
     * @param endDate Certificate will be valid to this date.
     * @return the new certificate.
     */
    fun createCert(
        root: X509Certificate,
        csr: PKCS10CertificationRequest,
        keyPair: KeyPair,
        startDate: DateTime,
        endDate: DateTime
    ): X509Certificate {
        val rootCertIssuer = X500Name("CN=root-cert")
        val issuedCertSerialNum = BigInteger(SecureRandom().nextLong().toString())
        val issuedCertBuilder = X509v3CertificateBuilder(
            rootCertIssuer, issuedCertSerialNum,
            startDate.toDate(), endDate.toDate(), csr.subject, csr.subjectPublicKeyInfo
        )
        val issuedCertExtUtils = JcaX509ExtensionUtils()

        // Add Extensions
        // Use BasicConstraints to say that this Cert is not a CA
        issuedCertBuilder.addExtension(Extension.basicConstraints, true, BasicConstraints(false))

        // Add Issuer cert identifier as Extension
        issuedCertBuilder.addExtension(Extension.authorityKeyIdentifier, false, issuedCertExtUtils.createAuthorityKeyIdentifier(root))
        issuedCertBuilder.addExtension(Extension.subjectKeyIdentifier, false, issuedCertExtUtils.createSubjectKeyIdentifier(csr.getSubjectPublicKeyInfo()))

        // Add intended key usage extension if needed
        issuedCertBuilder.addExtension(Extension.keyUsage, false, KeyUsage(KeyUsage.keyEncipherment))

        // Add DNS name is cert is to used for SSL
        issuedCertBuilder.addExtension(Extension.subjectAlternativeName, false, DERSequence(arrayOf<ASN1Encodable>(
            GeneralName(GeneralName.dNSName, "mydomain.local"),
            GeneralName(GeneralName.iPAddress, "127.0.0.1")
        )))

        val csrBuilder = JcaContentSignerBuilder(SIGNATURE_ALGORITHM)
        val signer: ContentSigner = csrBuilder.build(keyPair.private)

        val issuedCertHolder: X509CertificateHolder = issuedCertBuilder.build(signer)
        val issuedCert = JcaX509CertificateConverter().setProvider(BC_PROVIDER).getCertificate(issuedCertHolder)

        // Verify the issued cert signature against the root (issuer) cert
        issuedCert.verify(root.publicKey, BC_PROVIDER)
        return issuedCert
    }

    /** Loads any of the supported security objects from the internal store.
     * It can load [Key]s, [Certificate]s and Certificate Requests (CSR).
     * @param name Item name in the internal store.
     * @return The required object.
     */
    inline fun<reified T> load(name: String): T {
        return when (T::class) {
            PrivateKey::class -> loadKey(name)
            PublicKey::class -> loadKey(name)
            Certificate::class -> loadCertificate(name) as T
            X509Certificate::class -> loadCertificate(name) as T
            PKCS10CertificationRequest::class -> loadCertificateRequest(name) as T
            else -> throw RuntimeException("cannot load object of type: ${T::class}")
        }
    }

    /** Loads any of the supported security objects from an input stream.
     * It can load [Key]s, [Certificate]s and Certificate Requests (CSR).
     * @param input Input to read the object.
     * @return The required object.
     */
    inline fun<reified T> load(input: InputStream): T {
        return when (T::class) {
            PrivateKey::class -> loadKey(input)
            PublicKey::class -> loadKey(input)
            Certificate::class -> loadCertificate(input) as T
            X509Certificate::class -> loadCertificate(input) as T
            PKCS10CertificationRequest::class -> loadCertificateRequest(input) as T
            else -> throw RuntimeException("cannot load object of type: ${T::class}")
        }
    }

    /** Loads a public or private key from the internal store.
     * @param name Name of the key in the store.
     * @return the required key.
     */
    fun<T : Key> loadKey(name: String): T {
        val pemData: String = store.read<String>(name)?.content
            ?: throw RuntimeException("key not found in store: $name")
        return loadKey(pemData.byteInputStream())
    }

    /** Loads a public or private key from an InputStream.
     * The key is expected to be in PEM format.
     *
     * @param input Input to read the key in PEM format.
     * @return the required key.
     */
    @Suppress("UNCHECKED_CAST")
    fun<T : Key> loadKey(input: InputStream): T {
        val parser = PEMParser(input.reader())
        val keyFactory = KeyFactory.getInstance("RSA")

        return when (val keyInfo = parser.readObject()) {
            is PrivateKeyInfo -> keyFactory.generatePrivate(PKCS8EncodedKeySpec(keyInfo.encoded))
            is SubjectPublicKeyInfo -> keyFactory.generatePublic(X509EncodedKeySpec(keyInfo.encoded))
            else -> throw RuntimeException("unsupported PEM object")
        } as T
    }

    /** Loads a certificate from the internal store.
     * @param name Name of the certificate in the store.
     * @return the required key.
     */
    fun loadCertificate(name: String): Certificate {
        val pemData: String = store.read<String>(name)?.content
            ?: throw RuntimeException("certificate not found in store: $name")
        return loadCertificate(pemData.byteInputStream())
    }

    /** Loads an X509 certificate from an InputStream.
     * The certificate is expected to be in PEM format.
     *
     * @param input Input to read the certificate in PEM format.
     * @return the required certificate.
     */
    fun loadCertificate(input: InputStream): Certificate {
        val parser = PEMParser(input.reader())
        val pem: PemObject = parser.readPemObject()
        val holder = X509CertificateHolder(pem.content)
        return JcaX509CertificateConverter().setProvider(BC_PROVIDER).getCertificate(holder)
    }

    /** Loads a certificate from the internal store.
     * @param name Name of the certificate in the store.
     * @return the required key.
     */
    fun loadCertificateRequest(name: String): PKCS10CertificationRequest {
        val pemData: String = store.read<String>(name)?.content
            ?: throw RuntimeException("certificate request not found in store: $name")
        return loadCertificateRequest(pemData.byteInputStream())
    }

    /** Loads an X509 certificate from an InputStream.
     * The certificate is expected to be in PEM format.
     *
     * @param input Input to read the certificate in PEM format.
     * @return the required certificate.
     */
    fun loadCertificateRequest(input: InputStream): PKCS10CertificationRequest {
        val parser = PEMParser(input.reader())
        return parser.readObject() as PKCS10CertificationRequest
    }

    /** Saves a key in the internal store.
     * @param name Name of the key in the internal store.
     * @param key Key to save.
     */
    fun save(
        name: String,
        key: Key
    ) {
        val out = ByteArrayOutputStream().apply(key::writeTo)
        store.save(name, out.toByteArray().toString(Charset.defaultCharset()))
    }

    /** Saves a certificate request into the internal store.
     * @param name Name of the item in the internal store.
     * @param csr Certificate request to save.
     */
    fun save(
        name: String,
        csr: PKCS10CertificationRequest
    ) {
        val out = ByteArrayOutputStream().apply(csr::writeTo)
        store.save(name, out.toByteArray().toString(Charset.defaultCharset()))
    }

    /** Loads a keystore from the internal storage.
     *
     * It assumes the key store was saved using the [saveKeyStore] method.
     *
     * @param name KeyStore name in the internal storage.
     * @param password Password to open the keystore.
     * @return the required key store, or null if it doesn't exist.
     */
    fun loadKeyStore(
        name: String,
        password: String
    ): KeyStore? {
        val data: String = store.read<String>(name)?.content ?: return null
        val input = Base64.getDecoder().decode(data).inputStream()
        return KeyStore.getInstance(SecretsProvider.DEFAULT_KEYSTORE_TYPE).apply {
            load(input, password.toCharArray())
        }
    }

    /** Saves a key pair and a certificate into the internal storage and returns the key store.
     *
     * The [alias] parameter will be used as alias to add the private key and the certificate into the
     * key store. The public key will be stored under the alias: ${name}.publicKey
     *
     * If the key store already exists, it updates the entries with the key pair and certificate provided
     * in the parameters.
     *
     * @param name KeyStore name in the internal storage. Can be used to retrieve the key store with [loadKeyStore].
     * @param alias Alias to identify the private key and the certificate within the [KeyStore].
     * @param password Password to protect the key store.
     * @param keyPair Key pair to add to the key store.
     * @param certificate Certificate to add to the key store.
     *
     * @return the created or updated key store.
     */
    fun saveKeyStore(
        name: String,
        alias: String,
        password: String,
        keyPair: KeyPair,
        certificate: X509Certificate
    ): KeyStore {
        val keyStore: KeyStore = loadKeyStore(alias, password)
            ?: KeyStore.getInstance(STORE_TYPE, BC_PROVIDER).apply { load(null, null) }

        keyStore.setKeyEntry(alias, keyPair.private, password.toCharArray(), arrayOf(certificate))
        keyStore.setKeyEntry("${alias}.publicKey", keyPair.private, password.toCharArray(), arrayOf(certificate))

        val out = ByteArrayOutputStream().apply {
            keyStore.store(this, password.toCharArray())
        }
        val encodedData = Base64.getEncoder().encodeToString(out.toByteArray())
        store.save(name, encodedData)
        return keyStore
    }

//
//    fun main(args: Array<String>) {
//        // Add the BouncyCastle Provider
//        Security.addProvider(BouncyCastleProvider())
//
//        // Initialize a new KeyPair generator
//        val keyPairGenerator = KeyPairGenerator.getInstance(KEY_ALGORITHM, BC_PROVIDER)
//        keyPairGenerator.initialize(2048)
//
//        // Setup start date to yesterday and end date for 1 year validity
//        val calendar = Calendar.getInstance()
//        calendar.add(Calendar.DATE, -1)
//        val startDate = calendar.time
//        calendar.add(Calendar.YEAR, 1)
//        val endDate = calendar.time
//
//        // First step is to create a root certificate
//        // First Generate a KeyPair,
//        // then a random serial number
//        // then generate a certificate using the KeyPair
//        val rootKeyPair = keyPairGenerator.generateKeyPair()
//        val rootSerialNum = BigInteger(java.lang.Long.toString(SecureRandom().nextLong()))
//
//        // Issued By and Issued To same for root certificate
//        val rootCertIssuer = X500Name("CN=root-cert")
//        val rootCertContentSigner = JcaContentSignerBuilder(SIGNATURE_ALGORITHM).setProvider(BC_PROVIDER).build(rootKeyPair.private)
//        val rootCertBuilder: X509v3CertificateBuilder = JcaX509v3CertificateBuilder(rootCertIssuer, rootSerialNum, startDate, endDate, rootCertIssuer, rootKeyPair.public)
//
//        // Add Extensions
//        // A BasicConstraint to mark root certificate as CA certificate
//        val rootCertExtUtils = JcaX509ExtensionUtils()
//        rootCertBuilder.addExtension(Extension.basicConstraints, true, BasicConstraints(true))
//        rootCertBuilder.addExtension(Extension.subjectKeyIdentifier, false, rootCertExtUtils.createSubjectKeyIdentifier(rootKeyPair.public))
//
//        // Create a cert holder and export to X509Certificate
//        val rootCertHolder = rootCertBuilder.build(rootCertContentSigner)
//        val rootCert = JcaX509CertificateConverter().setProvider(BC_PROVIDER).getCertificate(rootCertHolder)
////        writeCertToFileBase64Encoded(rootCert, "root-cert.cer")
////        exportKeyPairToKeystoreFile(rootKeyPair, rootCert, "root-cert", "root-cert.pfx", "PKCS12", "pass")
//
//        // Generate a new KeyPair and sign it using the Root Cert Private Key
//        // by generating a CSR (Certificate Signing Request)
//        val issuedCertSubject = X500Name("CN=issued-cert")
//        val issuedCertSerialNum = BigInteger(java.lang.Long.toString(SecureRandom().nextLong()))
//        val issuedCertKeyPair = keyPairGenerator.generateKeyPair()
//        val p10Builder: PKCS10CertificationRequestBuilder = JcaPKCS10CertificationRequestBuilder(issuedCertSubject, issuedCertKeyPair.public)
//        val csrBuilder = JcaContentSignerBuilder(SIGNATURE_ALGORITHM).setProvider(BC_PROVIDER)
//
//        // Sign the new KeyPair with the root cert Private Key
//        val csrContentSigner = csrBuilder.build(rootKeyPair.private)
//        val csr = p10Builder.build(csrContentSigner)
//
//        // Use the Signed KeyPair and CSR to generate an issued Certificate
//        // Here serial number is randomly generated. In general, CAs use
//        // a sequence to generate Serial number and avoid collisions
//        val issuedCertBuilder = X509v3CertificateBuilder(rootCertIssuer, issuedCertSerialNum, startDate, endDate, csr.subject, csr.subjectPublicKeyInfo)
//        val issuedCertExtUtils = JcaX509ExtensionUtils()
//
//        // Add Extensions
//        // Use BasicConstraints to say that this Cert is not a CA
//        issuedCertBuilder.addExtension(Extension.basicConstraints, true, BasicConstraints(false))
//
//        // Add Issuer cert identifier as Extension
//        issuedCertBuilder.addExtension(Extension.authorityKeyIdentifier, false, issuedCertExtUtils.createAuthorityKeyIdentifier(rootCert))
//        issuedCertBuilder.addExtension(Extension.subjectKeyIdentifier, false, issuedCertExtUtils.createSubjectKeyIdentifier(csr.subjectPublicKeyInfo))
//
//        // Add intended key usage extension if needed
//        issuedCertBuilder.addExtension(Extension.keyUsage, false, KeyUsage(KeyUsage.keyEncipherment))
//
//        // Add DNS name is cert is to used for SSL
//        issuedCertBuilder.addExtension(Extension.subjectAlternativeName, false, DERSequence(arrayOf<ASN1Encodable>(
//            GeneralName(GeneralName.dNSName, "mydomain.local"),
//            GeneralName(GeneralName.iPAddress, "127.0.0.1")
//        )))
//        val issuedCertHolder = issuedCertBuilder.build(csrContentSigner)
//        val issuedCert = JcaX509CertificateConverter().setProvider(BC_PROVIDER).getCertificate(issuedCertHolder)
//
//        // Verify the issued cert signature against the root (issuer) cert
//        issuedCert.verify(rootCert.publicKey, BC_PROVIDER)
//    }
}
