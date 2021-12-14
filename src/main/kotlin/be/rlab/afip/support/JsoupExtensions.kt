package be.rlab.afip.support

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormatter
import org.joda.time.format.ISODateTimeFormat
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import java.text.NumberFormat

fun Element.nsSelect(selector: String): Elements {
    return select("*").fold(Elements()) { elements, element ->
        val tagName = element.tagName().substringAfter(":")
        if (tagName == selector) {
            elements += element
        }
        elements
    }
}

fun Element.string(cssQuery: String): String {
    val element = select(cssQuery)
    val text = element.text()
    require(text.isNotEmpty()) { "Unexpected empty element: ${element.html()}" }
    return text
}

fun Element.number(cssQuery: String): Number {
    val formatter = NumberFormat.getNumberInstance()
    return formatter.parse(string(cssQuery))
}

fun Element.dateTime(
    cssQuery: String,
    formatter: DateTimeFormatter = ISODateTimeFormat.dateTime()
): DateTime {
    return DateTime.parse(string(cssQuery), formatter)
}

fun Element.dateTimeMillis(
    cssQuery: String,
    formatter: DateTimeFormatter = ISODateTimeFormat.dateTime()
): Long {
    return dateTime(cssQuery, formatter).millis
}

fun Element.boolean(
    cssQuery: String,
    defaultValue: Boolean? = null
): Boolean {
    return when (val value = string(cssQuery)) {
        "S" -> true
        "N" -> false
        else -> defaultValue ?: throw RuntimeException("Invalid boolean response: $value")
    }
}

fun Document.serviceErrors(): List<ServiceError> {
    val errors = select("Errors")
    return if (errors.isNotEmpty()) {
        errors.select("Err").map { errorElement ->
            ServiceError(
                code = errorElement.number("Code").toInt(),
                message = errorElement.string("Msg"),
            )
        }
    } else {
        emptyList()
    }
}
