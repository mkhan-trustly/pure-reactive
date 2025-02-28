package se.work.reactive.application;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class AppConfig {

    @Value(("${app.traffic.url}"))
    private String url;

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl(url)
                .build();
    }

    /**
     * Modules:
     * ------------------------------------------------
     * ParameterNamesModule(JsonCreator.Mode.PROPERTIES)
     * Normally, Jackson requires explicit annotations(@JsonProperty) to map JSON fields to constructor parameters.
     * We want automatic mapping without needing @JsonProperty
     * ------------------------------------------------
     * new JavaTimeModule()
     * Jackson does not natively handle Java Time API well, and without this module, it may fail to serialize or
     * deserialize LocalDate & LocalDateTime
     * ------------------------------------------------
     * new AfterburnerModule()
     * Not required here, but in high-performance applications.
     * Optimises serialization/deserialization performance by using bytecode enhancements.
     * ------------------------------------------------
     * ALLOW_COERCION_OF_SCALARS - default true
     * Example:
     * class X {
     *     public int id; // id is coerced from "123" (String) → int
     *     public boolean active; // active is coerced from "true" (String) → boolean
     * }
     * We don't want this behaviour, we want strict type checking.
     * ------------------------------------------------
     * WRITE_DATES_AS_TIMESTAMPS - default true
     * Example:
     * timestamp will be 1709020200000, but we want "2024-02-27T10:30:00"
     * We want human-readable strings
     * ------------------------------------------------
     * FAIL_ON_UNKNOWN_PROPERTIES - default true
     * We want extra field to be ignored, usually it is the case but adjust accordingly.
     *
     */

    @Bean
    public ObjectMapper objectMapper() {
        return JsonMapper.builder()
                .addModules(
                        new JavaTimeModule(),
                        new ParameterNamesModule(JsonCreator.Mode.PROPERTIES)
                )
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .disable(MapperFeature.ALLOW_COERCION_OF_SCALARS)
                .build();
    }
}
