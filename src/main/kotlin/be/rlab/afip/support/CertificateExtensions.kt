package be.rlab.afip.support

import org.bouncycastle.openssl.jcajce.JcaPEMWriter
import org.bouncycastle.pkcs.PKCS10CertificationRequest
import org.bouncycastle.util.io.pem.PemObject
import java.io.OutputStream
import java.security.Key
import java.security.KeyStore
import java.security.PrivateKey
import java.security.PublicKey
import java.security.cert.Certificate

private fun writeTo(
    out: OutputStream,
    pem: PemObject
) {
    JcaPEMWriter(out.writer()).use { writer ->
        writer.writeObject(pem)
    }
}

fun KeyStore.writeTo(
    out: OutputStream,
    password: String
): KeyStore = apply {
    store(out, password.toCharArray())
}

@Suppress("UNCHECKED_CAST")
fun Key.writeTo(out: OutputStream) {
    val pemType = when (this) {
        is PrivateKey -> "PRIVATE KEY"
        is PublicKey -> "PUBLIC KEY"
        else -> throw RuntimeException("Cannot write this type of key")
    }
    writeTo(out, PemObject(pemType, encoded))
}

fun Certificate.writeTo(out: OutputStream): Certificate = apply {
    writeTo(out, PemObject("CERTIFICATE", encoded))
}

fun PKCS10CertificationRequest.writeTo(out: OutputStream): PKCS10CertificationRequest = apply {
    writeTo(out, PemObject("CERTIFICATE REQUEST", encoded))
}
