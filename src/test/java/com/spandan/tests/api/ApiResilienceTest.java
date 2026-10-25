package com.spandan.tests.api;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.spandan.framework.api.ApiClient;
import com.spandan.framework.api.BookingService;
import com.spandan.framework.api.models.Booking;
import com.spandan.framework.utils.TestDataReader;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.net.SocketTimeoutException;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.expectThrows;

/**
 * Service virtualization: a WireMock stub stands in for the Booking service so we can test
 * conditions the real service can't produce on demand (slowness, outages, contract breaks).
 */
@Epic("Booking API")
@Feature("Resilience with WireMock")
public class ApiResilienceTest {

    private static final int READ_TIMEOUT_MS = 2000;
    private static final String BOOKING_JSON =
            "{\"bookingid\":1,\"booking\":{\"firstname\":\"Spandan\",\"lastname\":\"Pandey\",\"totalprice\":4500,"
            + "\"depositpaid\":true,\"bookingdates\":{\"checkin\":\"2026-12-01\",\"checkout\":\"2026-12-05\"},"
            + "\"additionalneeds\":\"Breakfast\"}}";

    private WireMockServer stub;
    private BookingService service;

    @BeforeClass(alwaysRun = true)
    public void startStub() {
        stub = new WireMockServer(WireMockConfiguration.options().dynamicPort());
        stub.start();
        service = new BookingService(ApiClient.requestSpec(stub.baseUrl(), READ_TIMEOUT_MS));
    }

    @BeforeMethod(alwaysRun = true)
    public void resetStub() {
        stub.resetAll();
    }

    @AfterClass(alwaysRun = true)
    public void stopStub() {
        if (stub != null) {
            stub.stop();
        }
    }

    @Test(groups = {"api", "regression"})
    @Description("Regression guard for the 418/415 bug: the client must send plain JSON headers")
    public void clientSendsHeadersTheBookingApiAccepts() {
        stub.stubFor(WireMock.post("/booking").willReturn(WireMock.okJson(BOOKING_JSON)));

        service.createBooking(TestDataReader.read("booking.json", Booking.class))
                .then().statusCode(200);

        stub.verify(WireMock.postRequestedFor(WireMock.urlEqualTo("/booking"))
                .withHeader("Accept", WireMock.equalTo("application/json"))
                .withHeader("Content-Type", WireMock.containing("charset=UTF-8")));
    }

    @Test(groups = {"api", "regression"})
    @Description("A slow dependency fails fast with a timeout instead of hanging the suite")
    public void slowServiceTimesOutFast() {
        stub.stubFor(WireMock.get("/booking/1")
                .willReturn(WireMock.okJson(BOOKING_JSON).withFixedDelay(READ_TIMEOUT_MS * 3)));

        long start = System.currentTimeMillis();
        Throwable error = expectThrows(Throwable.class, () -> service.getBooking(1));
        long elapsed = System.currentTimeMillis() - start;

        assertTrue(hasCause(error, SocketTimeoutException.class),
                "Expected a SocketTimeoutException but got: " + error);
        assertTrue(elapsed < READ_TIMEOUT_MS * 2L,
                "Request should give up near the " + READ_TIMEOUT_MS + " ms timeout, took " + elapsed + " ms");
    }

    @Test(groups = {"api", "regression"})
    @Description("An outage is reported with its status and Retry-After header, not swallowed")
    public void serviceOutageIsReported() {
        stub.stubFor(WireMock.get("/booking/1")
                .willReturn(WireMock.aResponse().withStatus(503).withHeader("Retry-After", "30")));

        service.getBooking(1)
                .then()
                .statusCode(503)
                .header("Retry-After", "30");
    }

    @Test(groups = {"api", "regression"})
    @Description("Schema validation catches a contract break even when the status code is 200")
    public void schemaValidationCatchesContractBreak() {
        // Missing required fields: lastname, totalprice, depositpaid, bookingdates
        stub.stubFor(WireMock.get("/booking/1").willReturn(WireMock.okJson("{\"firstname\":\"Spandan\"}")));

        expectThrows(AssertionError.class, () -> service.getBooking(1)
                .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/booking-schema.json")));
    }

    private static boolean hasCause(Throwable error, Class<? extends Throwable> type) {
        for (Throwable t = error; t != null; t = t.getCause()) {
            if (type.isInstance(t)) {
                return true;
            }
        }
        return false;
    }
}
