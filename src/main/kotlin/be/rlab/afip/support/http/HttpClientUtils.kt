package be.rlab.afip.support.http

import okhttp3.CookieJar
import okhttp3.OkHttpClient
import okhttp3.tls.HandshakeCertificates
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit

object HttpClientUtils {
    private val factory = CertificateFactory.getInstance("X.509")
    private val certificates: HandshakeCertificates = HandshakeCertificates.Builder()
        .addTrustedCertificate(
            factory.generateCertificate(
                Thread.currentThread().contextClassLoader.getResourceAsStream("wsass-homo-afip-gob-ar.pem")
            ) as X509Certificate
        )
        .addPlatformTrustedCertificates()
        .build()

    fun newHttpClient(cookieJar: CookieJar = PersistentCookieJar()): HttpClient {
        val internalClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .cookieJar(cookieJar)
            .sslSocketFactory(certificates.sslSocketFactory(), certificates.trustManager)
            .build()
        return HttpClient(internalClient)
    }
}
