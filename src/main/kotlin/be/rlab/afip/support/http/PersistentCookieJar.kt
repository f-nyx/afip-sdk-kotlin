package be.rlab.afip.support.http

import be.rlab.afip.portal.PortalService
import be.rlab.afip.portal.ApiConfig
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl

class PersistentCookieJar : CookieJar {
    private val cookies: MutableMap<String, List<Cookie>> = mutableMapOf()

    fun clear() {
        cookies.clear()
    }

    fun updateAuthCookie(accessToken: String) {
        clear()
        saveFromResponse(ApiConfig.API_URL.toHttpUrl(), listOf(
            Cookie.Builder()
                .domain(ApiConfig.API_URL.toHttpUrl().host)
                .name(PortalService.SID_COOKIE)
                .value(accessToken)
                .build()
        ))
    }

    override fun saveFromResponse(
        url: HttpUrl,
        cookies: List<Cookie>
    ) {
        val resolvedCookies = (this.cookies[url.redact()] ?: emptyList()) + cookies
        this.cookies += url.redact() to resolvedCookies
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        return cookies[url.redact()] ?: emptyList()
    }
}
