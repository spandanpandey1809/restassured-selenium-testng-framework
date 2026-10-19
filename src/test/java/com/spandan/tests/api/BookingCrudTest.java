package com.spandan.tests.api;

import com.spandan.framework.api.models.Booking;
import com.spandan.framework.utils.TestDataReader;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.response.Response;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Map;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.notNullValue;

/**
 * End-to-end CRUD flow on one booking. Tests run in order via dependsOnMethods,
 * so a failure in "create" skips the rest instead of producing misleading failures.
 */
@Epic("Booking API")
@Feature("Booking CRUD")
public class BookingCrudTest extends BaseApiTest {

    private Booking booking;
    private String token;
    private int bookingId;

    @BeforeClass(alwaysRun = true)
    public void prepareData() {
        booking = TestDataReader.read("booking.json", Booking.class);
        token = bookingService.getValidToken();
    }

    @Test(groups = {"api", "smoke"})
    @Description("Create a booking and verify the response echoes the request")
    public void createBooking() {
        Response response = bookingService.createBooking(booking);

        response.then()
                .statusCode(200)
                .body("bookingid", greaterThan(0))
                .body("booking.firstname", equalTo(booking.getFirstname()))
                .body("booking.lastname", equalTo(booking.getLastname()))
                .body("booking.totalprice", equalTo(booking.getTotalprice()))
                .body("booking.bookingdates.checkin", equalTo(booking.getBookingdates().getCheckin()));

        bookingId = response.path("bookingid");
    }

    @Test(groups = {"api", "smoke"}, dependsOnMethods = "createBooking")
    @Description("Fetch the created booking and validate it against the JSON schema")
    public void getBookingMatchesSchema() {
        bookingService.getBooking(bookingId)
                .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/booking-schema.json"))
                .body("firstname", equalTo(booking.getFirstname()));
    }

    @Test(groups = {"api", "regression"}, dependsOnMethods = "createBooking")
    @Description("Created booking is returned when filtering by name")
    public void filterBookingsByName() {
        bookingService.getBookingIds(Map.of(
                        "firstname", booking.getFirstname(),
                        "lastname", booking.getLastname()))
                .then()
                .statusCode(200)
                .body("bookingid", hasItem(bookingId));
    }

    @Test(groups = {"api", "regression"}, dependsOnMethods = "getBookingMatchesSchema")
    @Description("Full update (PUT) replaces all booking fields")
    public void updateBooking() {
        Booking updated = Booking.builder()
                .firstname("Updated")
                .lastname("Name")
                .totalprice(9999)
                .depositpaid(false)
                .bookingdates("2027-01-10", "2027-01-15")
                .additionalneeds("Late checkout")
                .build();

        bookingService.updateBooking(bookingId, updated, token)
                .then()
                .statusCode(200)
                .body("firstname", equalTo("Updated"))
                .body("totalprice", equalTo(9999))
                .body("depositpaid", equalTo(false));
    }

    @Test(groups = {"api", "regression"}, dependsOnMethods = "updateBooking")
    @Description("Partial update (PATCH) changes only the given fields")
    public void partialUpdateBooking() {
        bookingService.partialUpdateBooking(bookingId, Map.of("additionalneeds", "Airport pickup"), token)
                .then()
                .statusCode(200)
                .body("additionalneeds", equalTo("Airport pickup"))
                .body("firstname", equalTo("Updated"));
    }

    // In the smoke suite the update tests are excluded, so their dependency is skipped there.
    @Test(groups = {"api", "smoke"},
            dependsOnMethods = {"getBookingMatchesSchema", "partialUpdateBooking"},
            ignoreMissingDependencies = true)
    @Description("Delete the booking, then confirm it no longer exists")
    public void deleteBooking() {
        // Note: this API returns 201 Created for a successful delete.
        bookingService.deleteBooking(bookingId, token)
                .then()
                .statusCode(201);

        bookingService.getBooking(bookingId)
                .then()
                .statusCode(404);
    }

    @Test(groups = {"api", "regression"})
    @Description("Booking list endpoint returns ids")
    public void listBookingsReturnsIds() {
        bookingService.getBookingIds(Map.of())
                .then()
                .statusCode(200)
                .body("[0].bookingid", notNullValue());
    }
}
