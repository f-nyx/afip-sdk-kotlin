package be.rlab.afip.apps.certs

import org.joda.time.DateTime
import java.security.cert.Certificate
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate

data class CertificateInfo(
    val cuit: Long,
    val alias: String,
    val serialNumber: String,
    val commonName: String,
    val encodedCertificate: String?,
    val organizationName: String?,
    val organizationUnit: String?,
    val location: String?,
    val country: String?,
    val issuedAt: DateTime,
    val expiresAt: DateTime,
    val valid: Boolean
) {
    companion object {
        private val factory = CertificateFactory.getInstance("X.509")
    }

    fun certificate(): Certificate? {
        return encodedCertificate?.let {
            factory.generateCertificate(encodedCertificate.byteInputStream()) as X509Certificate
        }
    }
}
