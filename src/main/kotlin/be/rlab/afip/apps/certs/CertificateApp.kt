package be.rlab.afip.apps.certs

import be.rlab.afip.apps.AppsService
import be.rlab.afip.apps.AppConfig
import be.rlab.afip.apps.PortalApp
import be.rlab.afip.apps.PortalAppBuilder
import be.rlab.afip.support.number
import be.rlab.afip.support.string
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.joda.time.format.DateTimeFormat

class CertificateApp(
    client: AppsService,
    config: AppConfig
) : PortalApp(client, config) {
    companion object Builder : PortalAppBuilder {
        private val BASE_URL: String = "https://wsass-homo.afip.gob.ar/wsass/portal/Autoservicio"
        private val INTRODUCTION_URL = "$BASE_URL/introduccion.aspx".toHttpUrl()
        private val LIST_CERTS_URL = "$BASE_URL/certificadosdecuit.aspx".toHttpUrl()
        private val SHOW_CERT_URL = "$BASE_URL/vercertificadodecuit.aspx?cuit=$0&alias=$1"
        private val dateFormatter = DateTimeFormat.forPattern("dd/MM/yyyy hh:mm:ss")

        override fun new(
            appsService: AppsService,
            config: AppConfig
        ): PortalApp = CertificateApp(appsService, config)
    }

    fun listCertificates(): List<CertificateInfo> = loginIfRequired {
        httpClient.get(LIST_CERTS_URL).select("#gvCertificados tr")
            .filter { row -> row.select("td").size == 4 }
            .map { row ->
                val cuit = row.number("td:nth-child(2)")
                val alias = row.string("td:nth-child(3)")
                val dn = row.string("td:nth-child(4)")
                readCertificate(cuit.toLong(), alias, dn)
            }
    }

    override fun loginRequired(): Boolean {
        return httpClient.get(INTRODUCTION_URL).select("#hlIniciarSession").isNotEmpty()
    }

    private fun readCertificate(
        cuit: Long,
        alias: String,
        dn: String
    ): CertificateInfo {
        val certViewDoc = httpClient.get(SHOW_CERT_URL
            .replace("$0", cuit.toString())
            .replace("$1", alias)
            .toHttpUrl()
        )
        val row = certViewDoc.selectFirst("#gvCertificados tr:nth-child(2)")
            ?: throw RuntimeException("Cannot find certificate information row: CUIT $cuit, ALIAS $alias")
        val issuedAt = row.string("td:nth-child(2)")
        val expiresAt = row.string("td:nth-child(3)")
        val dnFields = parseDn(dn)

        return CertificateInfo(
            cuit = cuit,
            alias = alias,
            serialNumber = row.string("td:nth-child(5)"),
            commonName = dnFields.getValue("CN"),
            encodedCertificate = parseCert(certViewDoc.select("#txtCert font").html()),
            organizationName = dnFields["O"],
            organizationUnit = dnFields["OU"],
            location = dnFields["ST"],
            country = dnFields["C"],
            issuedAt = dateFormatter.parseDateTime(issuedAt.substringBeforeLast(" ")),
            expiresAt = dateFormatter.parseDateTime(expiresAt.substringBeforeLast(" ")),
            valid = row.string("td:nth-child(4)") == "VALID"
        )
    }

    private fun parseDn(dn: String): Map<String, String> {
        return dn.split(",").associate { field ->
            val (name, value) = field.trim().split("=")
            name.trim() to value.trim()
        }
    }

    private fun parseCert(certHtml: String): String? {
        val cert: String = certHtml.replace("<br>", "\n")

        return cert.takeIf {
            if (cert.trim().isEmpty()) {
                false
            } else {
                val lines: List<String> = cert.split("\n")
                // Validates that the certificate data is valid
                lines.first() == "-----BEGIN CERTIFICATE-----" && lines.last() == "-----END CERTIFICATE-----"
            }
        }
    }
}
