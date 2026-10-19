package com.spandan.tests.api;

import com.spandan.framework.api.BookingService;
import org.testng.annotations.BeforeClass;

public abstract class BaseApiTest {

    protected BookingService bookingService;

    @BeforeClass(alwaysRun = true)
    public void setUpService() {
        bookingService = new BookingService();
    }
}
