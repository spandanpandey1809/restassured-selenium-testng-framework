package com.spandan.framework.api.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Booking {

    private String firstname;
    private String lastname;
    private int totalprice;
    private boolean depositpaid;
    private BookingDates bookingdates;
    private String additionalneeds;

    public Booking() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public int getTotalprice() {
        return totalprice;
    }

    public void setTotalprice(int totalprice) {
        this.totalprice = totalprice;
    }

    public boolean isDepositpaid() {
        return depositpaid;
    }

    public void setDepositpaid(boolean depositpaid) {
        this.depositpaid = depositpaid;
    }

    public BookingDates getBookingdates() {
        return bookingdates;
    }

    public void setBookingdates(BookingDates bookingdates) {
        this.bookingdates = bookingdates;
    }

    public String getAdditionalneeds() {
        return additionalneeds;
    }

    public void setAdditionalneeds(String additionalneeds) {
        this.additionalneeds = additionalneeds;
    }

    /** Fluent builder so tests read clearly: Booking.builder().firstname("A").build() */
    public static final class Builder {
        private final Booking booking = new Booking();

        public Builder firstname(String v) { booking.firstname = v; return this; }
        public Builder lastname(String v) { booking.lastname = v; return this; }
        public Builder totalprice(int v) { booking.totalprice = v; return this; }
        public Builder depositpaid(boolean v) { booking.depositpaid = v; return this; }
        public Builder bookingdates(String checkin, String checkout) {
            booking.bookingdates = new BookingDates(checkin, checkout);
            return this;
        }
        public Builder additionalneeds(String v) { booking.additionalneeds = v; return this; }
        public Booking build() { return booking; }
    }
}
