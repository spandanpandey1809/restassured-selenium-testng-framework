package com.spandan.framework.api;

import com.spandan.framework.config.ConfigManager;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.specification.RequestSpecification;

/** Shared Rest Assured request setup: base URL, JSON headers, logging and Allure request/response capture. */
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

    public static RequestSpecification requestSpec() {
        return new RequestSpecBuilder()
                .setBaseUri(ConfigManager.get("api.base.url"))
                .setContentType(JSON_UTF8)
                .setAccept(JSON)
                .addFilter(new AllureRestAssured())
                .log(LogDetail.METHOD)
                .log(LogDetail.URI)
                .build();
    }
}
