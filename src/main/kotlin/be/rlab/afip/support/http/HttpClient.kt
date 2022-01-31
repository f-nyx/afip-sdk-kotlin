package be.rlab.afip.support.http

import be.rlab.afip.support.ObjectMapperFactory
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.FormBody
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class HttpClient(
    internal val internalClient: OkHttpClient
) {
    private val objectMapper: ObjectMapper = ObjectMapperFactory.defaultObjectMapper()

    fun get(url: HttpUrl): Document {
        val response: String = internalClient.newCall(
            Request.Builder()
                .url(url)
                .get()
                .build()
        ).execute().body?.string() ?: throw RuntimeException("Cannot read response body")
        return Jsoup.parse(response)
    }

    fun<T : Any> getJson(
        url: HttpUrl,
        reader: ((JsonNode) -> T)? = null
    ): T {
        val response: String = internalClient.newCall(
            Request.Builder()
                .url(url)
                .get()
                .build()
        ).execute().body?.string() ?: throw RuntimeException("Cannot read response body")
        return reader?.invoke(objectMapper.readTree(response))
            ?: objectMapper.readValue(response, object : TypeReference<T>() { })
    }

    fun postForm(
        url: HttpUrl,
        data: Map<String, String>
    ): Document {
        val response: String = internalClient.newCall(
            Request.Builder()
                .url(url)
                .post(
                    data.entries.fold(FormBody.Builder()) { builder, (name, value) ->
                        builder.addEncoded(name, value)
                    }.build()
                )
                .build()
        ).execute().body?.string() ?: throw RuntimeException("Cannot read response body")

        return Jsoup.parse(response)
    }
}
