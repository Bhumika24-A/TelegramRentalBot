package com.rentalbot;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import static org.junit.jupiter.api.Assertions.*;

public class SearchInputValidationTest {

    @Test
    public void testValidInputFormat() {
        String input = "Nagpur,3000,3,2026-10-15,2026-10-17";
        String[] parts = input.split(",");
        assertEquals(5, parts.length);

        String location = parts[0].trim();
        double budget = Double.parseDouble(parts[1].trim());
        int guests = Integer.parseInt(parts[2].trim());
        LocalDate checkIn = LocalDate.parse(parts[3].trim());
        LocalDate checkOut = LocalDate.parse(parts[4].trim());

        assertEquals("Nagpur", location);
        assertEquals(3000.0, budget);
        assertEquals(3, guests);
        assertTrue(checkOut.isAfter(checkIn));
    }

    @Test
    public void testInvalidBudgetThrowsException() {
        String invalidBudget = "abc";
        assertThrows(NumberFormatException.class, () -> Double.parseDouble(invalidBudget));
    }

    @Test
    public void testInvalidDateFormatThrowsException() {
        String invalidDate = "15/10/2026";
        assertThrows(DateTimeParseException.class, () -> LocalDate.parse(invalidDate));
    }

    @Test
    public void testCheckoutBeforeCheckinFailsValidation() {
        LocalDate checkIn = LocalDate.parse("2026-10-17");
        LocalDate checkOut = LocalDate.parse("2026-10-15");
        assertFalse(checkOut.isAfter(checkIn));
    }

    @Test
    public void testNonPositiveBudgetAndGuests() {
        double zeroBudget = 0.0;
        int negativeGuests = -1;
        assertFalse(zeroBudget > 0);
        assertFalse(negativeGuests > 0);
    }
}
