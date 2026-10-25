package com.spandan.framework.api;

import com.spandan.framework.config.ConfigManager;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.filter.log.LogDetail;
import io.restassured.specification.RequestSpecification;

/** Shared Rest Assured request setup: base URL, JSON headers, timeouts, logging and Allure capture. */
public final class ApiClient {

    /*
     * Explicit header values instead of ContentType.JSON:
     * - ContentType.JSON as Accept sends four media types, and this API answers "418 I'm a Teapot" to that.
     * - The API rejects any charset other than UTF-8 with "415 Unsupported Media Type".
     */
    private static final String JSON_UTF8 = "application/json; charset=UTF-8";
    private static final String JSON = "application/json";

    private ApiClient() {
    }

    /** Spec for the real Booking API, with timeouts from config. */
    public static RequestSpecification requestSpec() {
        return requestSpec(ConfigManager.get("api.base.url"), ConfigManager.getInt("api.read.timeout.ms"));
    }

    /**
     * Spec for any base URL. Without timeouts a slow dependency makes a test hang until CI kills the job;
     * with them it fails fast with a clear SocketTimeoutException.
     */
    public static RequestSpecification requestSpec(String baseUri, int readTimeoutMs) {
        RestAssuredConfig config = RestAssuredConfig.config().httpClient(HttpClientConfig.httpClientConfig()
                .setParam("http.connection.timeout", ConfigManager.getInt("api.connect.timeout.ms"))
                .setParam("http.socket.timeout", readTimeoutMs));

        return new RequestSpecBuilder()
                .setBaseUri(baseUri)
                .setConfig(config)
                .setContentType(JSON_UTF8)
                .setAccept(JSON)
                .addFilter(new AllureRestAssured())
                .log(LogDetail.METHOD)
                .log(LogDetail.URI)
                .build();
    }
}
