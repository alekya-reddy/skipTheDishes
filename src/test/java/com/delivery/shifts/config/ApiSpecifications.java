package com.delivery.shifts.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

public final class ApiSpecifications {
    private ApiSpecifications() {
    }

    public static RequestSpecification request() {
        return new RequestSpecBuilder()
                .setBaseUri(TestConfig.apiBaseUrl())
                .setBasePath("/api")
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .setConfig(RestAssuredConfig.config().objectMapperConfig(
                        new ObjectMapperConfig().jackson2ObjectMapperFactory((type, charset) ->
                                new ObjectMapper()
                                        .registerModule(new JavaTimeModule())
                                        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS))))
                .addFilter(new AllureHttpFilter())
                .addFilter(new RequestLoggingFilter())
                .addFilter(new ResponseLoggingFilter())
                .build();
    }

    public static ResponseSpecification jsonResponse() {
        return new ResponseSpecBuilder()
                .expectContentType(ContentType.JSON)
                .build();
    }
}
