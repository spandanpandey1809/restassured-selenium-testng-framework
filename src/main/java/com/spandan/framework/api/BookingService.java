package com.spandan.framework.api;

import com.spandan.framework.api.models.Booking;
import com.spandan.framework.config.ConfigManager;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.Map;

import static io.restassured.RestAssured.given;

/**
 * Service layer for the Booking API. Tests call these methods instead of building
 * requests themselves, so endpoint changes are fixed in one place.
 */
public class BookingService {

    private static final String AUTH = "/auth";
    private static final String BOOKING = "/booking";
    private static final String BOOKING_BY_ID = "/booking/{id}";

    private final RequestSpecification spec;

    /** Talks to the real Booking API configured in qa.properties. */
    public BookingService() {
        this(ApiClient.requestSpec());
    }

    /** Talks to any compatible endpoint, e.g. a WireMock stub of the Booking API. */
    public BookingService(RequestSpecification spec) {
        this.spec = spec;
    }

    @Step("Create auth token")
    public Response createToken(String username, String password) {
        return given().spec(spec)
                .body(Map.of("username", username, "password", password))
                .post(AUTH);
    }

    public String getValidToken() {
        return createToken(ConfigManager.get("api.username"), ConfigManager.get("api.password"))
                .then().statusCode(200)
                .extract().path("token");
    }

    @Step("Create booking")
    public Response createBooking(Booking booking) {
        return given().spec(spec)
                .body(booking)
                .post(BOOKING);
    }

    @Step("Get booking {id}")
    public Response getBooking(int id) {
        return given().spec(spec)
                .pathParam("id", id)
                .get(BOOKING_BY_ID);
    }

    @Step("Get booking ids filtered by {filters}")
    public Response getBookingIds(Map<String, ?> filters) {
        return given().spec(spec)
                .queryParams(filters)
                .get(BOOKING);
    }

    @Step("Update booking {id}")
    public Response updateBooking(int id, Booking booking, String token) {
        return given().spec(spec)
                .cookie("token", token)
                .pathParam("id", id)
                .body(booking)
                .put(BOOKING_BY_ID);
    }

    @Step("Partially update booking {id}")
    public Response partialUpdateBooking(int id, Map<String, ?> fields, String token) {
        return given().spec(spec)
                .cookie("token", token)
                .pathParam("id", id)
                .body(fields)
                .patch(BOOKING_BY_ID);
    }

    @Step("Delete booking {id}")
    public Response deleteBooking(int id, String token) {
        return given().spec(spec)
                .cookie("token", token)
                .pathParam("id", id)
                .delete(BOOKING_BY_ID);
    }
}
