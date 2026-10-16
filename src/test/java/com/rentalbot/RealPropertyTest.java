package com.rentalbot;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RealPropertyTest {

    @Test
    public void testPropertyCreationAndGetters() {
        RealProperty property = new RealProperty(
                "Grand Hotel",
                "Nagpur, IN",
                2500.0,
                8.5,
                "Deluxe Room",
                "https://booking.com/example"
        );

        assertEquals("Grand Hotel", property.getName());
        assertEquals("Nagpur, IN", property.getLocation());
        assertEquals(2500.0, property.getPrice(), 0.001);
        assertEquals(8.5, property.getRating(), 0.001);
        assertEquals("Deluxe Room", property.getRoomType());
        assertEquals("https://booking.com/example", property.getBookingUrl());
    }

    @Test
    public void testPropertyWithEmptyUrl() {
        RealProperty property = new RealProperty(
                "Budget Stay",
                "Goa, IN",
                1200.0,
                7.2,
                "Standard Room",
                ""
        );

        assertEquals("Budget Stay", property.getName());
        assertEquals("", property.getBookingUrl());
    }
}
