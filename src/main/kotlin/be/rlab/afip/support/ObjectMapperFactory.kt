package be.rlab.afip.support

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator
import com.fasterxml.jackson.datatype.joda.JodaModule
import com.fasterxml.jackson.module.kotlin.KotlinModule

object ObjectMapperFactory {

    val snakeCaseMapper: ObjectMapper = defaultObjectMapper()
        .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE )

    val anySupportMapper: ObjectMapper = snakeCaseMapper
        .activateDefaultTypingAsProperty(
            BasicPolymorphicTypeValidator.builder()
                .allowIfBaseType(Any::class.java)
                .build()
            , ObjectMapper.DefaultTyping.EVERYTHING, "_type")

    private fun defaultObjectMapper(): ObjectMapper {
        return configure(ObjectMapper())
    }

    private fun configure(objectMapper: ObjectMapper): ObjectMapper {
        return objectMapper
            .registerModule(JodaModule())
            .registerModule(KotlinModule.Builder().build())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .configure(SerializationFeature.INDENT_OUTPUT, true)
    }
}