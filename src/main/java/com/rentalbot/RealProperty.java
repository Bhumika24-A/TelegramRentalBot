package com.rentalbot;

public class RealProperty {

    private String name;
    private String location;
    private double price;
    private double rating;
    private String roomType;
    private String bookingUrl;

    public RealProperty(
            String name,
            String location,
            double price,
            double rating,
            String roomType,
            String bookingUrl) {

        this.name = name;
        this.location = location;
        this.price = price;
        this.rating = rating;
        this.roomType = roomType;
        this.bookingUrl = bookingUrl;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public double getPrice() {
        return price;
    }

    public double getRating() {
        return rating;
    }

    public String getRoomType() {
        return roomType;
    }

    public String getBookingUrl() {
        return bookingUrl;
    }
}