package com.rentalbot;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PropertyApiService {

    private final String apiKey;
    private final String apiHost;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public PropertyApiService(
            String apiKey,
            String apiHost) {

        this.apiKey = apiKey;
        this.apiHost = apiHost;

        this.httpClient =
                HttpClient.newHttpClient();

        this.objectMapper =
                new ObjectMapper();
    }

    public List<RealProperty> searchProperties(
            String location,
            double budget,
            int guests,
            LocalDate checkIn,
            LocalDate checkOut) {

        List<RealProperty> properties =
                new ArrayList<>();

        try {

            // ------------------------------------------------
            // STEP 1: Find destination ID
            // ------------------------------------------------

            String destinationUrl =
                    "https://"
                    + apiHost
                    + "/api/v1/hotels/searchDestination?query="
                    + encode(location);

            HttpRequest destinationRequest =
                    HttpRequest.newBuilder()
                            .uri(URI.create(destinationUrl))
                            .header(
                                    "Content-Type",
                                    "application/json"
                            )
                            .header(
                                    "x-rapidapi-host",
                                    apiHost
                            )
                            .header(
                                    "x-rapidapi-key",
                                    apiKey
                            )
                            .GET()
                            .build();

            HttpResponse<String> destinationResponse =
                    httpClient.send(
                            destinationRequest,
                            HttpResponse.BodyHandlers.ofString()
                    );

            System.out.println(
                    "Destination API Status: "
                    + destinationResponse.statusCode()
            );

            if (destinationResponse.statusCode() != 200) {

                System.out.println(
                        "Destination API Error:"
                );

                System.out.println(
                        destinationResponse.body()
                );

                return properties;
            }

            JsonNode destinationJson =
                    objectMapper.readTree(
                            destinationResponse.body()
                    );

            JsonNode destinationData =
                    destinationJson.path("data");

            if (!destinationData.isArray()
                    || destinationData.isEmpty()) {

                System.out.println(
                        "No destination found for: "
                        + location
                );

                return properties;
            }

            JsonNode firstDestination =
                    destinationData.get(0);

            String destinationId =
                    firstDestination
                            .path("dest_id")
                            .asText("");

            String searchType =
                    firstDestination
                            .path("search_type")
                            .asText("city");

            System.out.println(
                    "Destination ID: "
                    + destinationId
            );

            System.out.println(
                    "Search Type: "
                    + searchType
            );

            if (destinationId.isBlank()) {

                System.out.println(
                        "Destination ID is missing."
                );

                return properties;
            }

            // ------------------------------------------------
            // STEP 2: Search hotels
            // ------------------------------------------------

            String hotelUrl =
                    "https://"
                    + apiHost
                    + "/api/v1/hotels/searchHotels"
                    + "?dest_id="
                    + encode(destinationId)
                    + "&search_type="
                    + encode(searchType)
                    + "&arrival_date="
                    + checkIn
                    + "&departure_date="
                    + checkOut
                    + "&adults="
                    + guests
                    + "&room_qty=1"
                    + "&page_number=1"
                    + "&units=metric"
                    + "&temperature_unit=c"
                    + "&languagecode=en-us"
                    + "&currency_code=INR";

            System.out.println(
                    "Hotel Search URL:"
            );

            System.out.println(
                    hotelUrl
            );

            HttpRequest hotelRequest =
                    HttpRequest.newBuilder()
                            .uri(URI.create(hotelUrl))
                            .header(
                                    "Content-Type",
                                    "application/json"
                            )
                            .header(
                                    "x-rapidapi-host",
                                    apiHost
                            )
                            .header(
                                    "x-rapidapi-key",
                                    apiKey
                            )
                            .GET()
                            .build();

            HttpResponse<String> hotelResponse =
                    httpClient.send(
                            hotelRequest,
                            HttpResponse.BodyHandlers.ofString()
                    );

            System.out.println(
                    "Hotel API Status: "
                    + hotelResponse.statusCode()
            );

            if (hotelResponse.statusCode() != 200) {

                System.out.println(
                        "Hotel API Error:"
                );

                System.out.println(
                        hotelResponse.body()
                );

                return properties;
            }

            // ------------------------------------------------
            // STEP 3: Read JSON response
            // ------------------------------------------------

            JsonNode hotelJson =
                    objectMapper.readTree(
                            hotelResponse.body()
                    );

            if (!hotelJson.path("status").asBoolean(false)) {

                System.out.println(
                        "Hotel API returned unsuccessful status."
                );

                System.out.println(
                        hotelResponse.body()
                );

                return properties;
            }

            JsonNode data =
                    hotelJson.path("data");

            JsonNode hotels =
                    data.path("hotels");

            if (!hotels.isArray()) {

                System.out.println(
                        "Hotel list not found in API response."
                );

                return properties;
            }

            System.out.println(
                    "Hotels received: "
                    + hotels.size()
            );

            // ------------------------------------------------
            // STEP 4: Convert API data into RealProperty
            // ------------------------------------------------

            for (JsonNode hotel : hotels) {

                JsonNode property =
                        hotel.path("property");

                String name =
                        property
                                .path("name")
                                .asText("Unknown Property");

                String countryCode =
                        property
                                .path("countryCode")
                                .asText("");

                String hotelLocation =
                        location;

                if (!countryCode.isBlank()) {

                    hotelLocation =
                            location
                            + ", "
                            + countryCode;
                }

                double price =
                        getPrice(hotel);

                double rating =
                        property
                                .path("reviewScore")
                                .asDouble(0);

                String roomType =
                        getRoomType(hotel);

                String bookingUrl =
                        getBookingUrl(hotel);

                System.out.println(
                        "--------------------------------"
                );

                System.out.println(
                        "Property: "
                        + name
                );

                System.out.println(
                        "Price: "
                        + price
                );

                System.out.println(
                        "Rating: "
                        + rating
                );

                System.out.println(
                        "Room: "
                        + roomType
                );

                // ------------------------------------------------
                // STEP 5: Apply budget filter
                // ------------------------------------------------

                if (price > 0
                        && price <= budget) {

                    RealProperty realProperty =
                            new RealProperty(
                                    name,
                                    hotelLocation,
                                    price,
                                    rating,
                                    roomType,
                                    bookingUrl
                            );

                    properties.add(
                            realProperty
                    );
                }
            }

            // ------------------------------------------------
            // STEP 6: Sort by price
            // ------------------------------------------------

            properties.sort(
                    Comparator.comparingDouble(
                            RealProperty::getPrice
                    )
            );

            System.out.println(
                    "Matching properties: "
                    + properties.size()
            );

        }

        catch (Exception e) {

            System.out.println(
                    "Error while searching properties:"
            );

            e.printStackTrace();
        }

        return properties;
    }

    // ========================================================
    // GET PRICE
    // ========================================================

    private double getPrice(JsonNode hotel) {

        // Current API structure:
        //
        // hotel
        //   -> property
        //       -> priceBreakdown
        //           -> grossPrice
        //               -> value

        JsonNode property =
                hotel.path("property");

        JsonNode priceBreakdown =
                property.path("priceBreakdown");

        JsonNode grossPrice =
                priceBreakdown.path("grossPrice");

        if (grossPrice.has("value")) {

            return grossPrice
                    .path("value")
                    .asDouble(0);
        }

        // ------------------------------------------------
        // Fallback for another API response format
        // ------------------------------------------------

        JsonNode oldBreakdown =
                hotel.path("price_breakdown");

        JsonNode oldGrossPrice =
                oldBreakdown.path("gross_price");

        if (oldGrossPrice.has("value")) {

            return oldGrossPrice
                    .path("value")
                    .asDouble(0);
        }

        if (oldGrossPrice.isNumber()) {

            return oldGrossPrice.asDouble();
        }

        // ------------------------------------------------
        // Other possible fields
        // ------------------------------------------------

        String[] fields = {

                "min_total_price",
                "price",
                "min_price",
                "gross_price",
                "total_price",
                "display_price"
        };

        for (String field : fields) {

            JsonNode price =
                    hotel.path(field);

            if (price.isNumber()) {

                return price.asDouble();
            }

            if (price.isTextual()) {

                try {

                    return Double.parseDouble(
                            price.asText()
                    );

                }

                catch (Exception ignored) {
                }
            }
        }

        return 0;
    }

    // ========================================================
    // GET ROOM TYPE
    // ========================================================

    private String getRoomType(JsonNode hotel) {

        JsonNode property =
                hotel.path("property");

        String recommended =
                property
                        .path(
                                "recommendedUnitsConfigurationLabel"
                        )
                        .asText("");

        if (!recommended.isBlank()) {

            return recommended
                    .replaceAll("<[^>]*>", "")
                    .trim();
        }

        // ------------------------------------------------
        // Fallback fields
        // ------------------------------------------------

        String[] fields = {

                "accommodation_type_name",
                "room_type",
                "property_type",
                "accommodationType",
                "type"
        };

        for (String field : fields) {

            String value =
                    property
                            .path(field)
                            .asText("");

            if (!value.isBlank()) {

                return value;
            }
        }

        return "Accommodation";
    }

    // ========================================================
    // GET BOOKING URL
    // ========================================================

    private String getBookingUrl(JsonNode hotel) {

        JsonNode property =
                hotel.path("property");

        String url =
                property
                        .path("url")
                        .asText("");

        if (!url.isBlank()) {

            return url;
        }

        url =
                hotel
                        .path("url")
                        .asText("");

        if (!url.isBlank()) {

            return url;
        }

        return "";
    }

    // ========================================================
    // URL ENCODING
    // ========================================================

    private String encode(String value) {
        if (value == null) {
            return "";
        }
        return java.net.URLEncoder
                .encode(
                        value,
                        java.nio.charset.StandardCharsets.UTF_8
                );
    }
}