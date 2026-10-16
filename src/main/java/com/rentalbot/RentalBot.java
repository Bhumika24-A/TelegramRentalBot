package com.rentalbot;

import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.time.LocalDate;
import java.util.List;

public class RentalBot
        implements LongPollingUpdateConsumer {

    private final TelegramClient telegramClient;

    private final PropertyApiService propertyApiService;

    public RentalBot(
            TelegramClient telegramClient,
            PropertyApiService propertyApiService) {

        this.telegramClient =
                telegramClient;

        this.propertyApiService =
                propertyApiService;
    }

    @Override
    public void consume(List<Update> updates) {

        for (Update update : updates) {

            if (!update.hasMessage()) {
                continue;
            }

            if (!update.getMessage().hasText()) {
                continue;
            }

            String message =
                    update.getMessage().getText();

            long chatId =
                    update.getMessage().getChatId();

            // -----------------------------------------
            // START COMMAND
            // -----------------------------------------

            if (message.equalsIgnoreCase("/start")) {

                sendMessage(
                        chatId,

                        "🏠 Welcome to Real Property Assistant!\n\n"
                        + "I can search actual accommodation listings.\n\n"
                        + "Use /search to start."
                );
            }

            // -----------------------------------------
            // SEARCH COMMAND
            // -----------------------------------------

            else if (
                    message.equalsIgnoreCase("/search")
            ) {

                sendMessage(
                        chatId,

                        "🔎 Real Property Search\n\n"
                        + "Enter:\n\n"
                        + "location,budget,guests,checkin,checkout\n\n"
                        + "Example:\n"
                        + "Nagpur,3000,3,2026-10-15,2026-10-17"
                );
            }

            // -----------------------------------------
            // SEARCH INPUT
            // -----------------------------------------

            else {

                processSearch(
                        chatId,
                        message
                );
            }
        }
    }

    private void processSearch(
            long chatId,
            String message) {

        try {

            String[] parts =
                    message.split(",");

            // -----------------------------------------
            // VALIDATE INPUT
            // -----------------------------------------

            if (parts.length != 5) {

                sendMessage(
                        chatId,

                        "❌ Invalid format.\n\n"
                        + "Use:\n"
                        + "location,budget,guests,checkin,checkout\n\n"
                        + "Example:\n"
                        + "Nagpur,3000,3,2026-10-15,2026-10-17"
                );

                return;
            }

            String location =
                    parts[0].trim();

            double budget =
                    Double.parseDouble(
                            parts[1].trim()
                    );

            int guests =
                    Integer.parseInt(
                            parts[2].trim()
                    );

            LocalDate checkIn =
                    LocalDate.parse(
                            parts[3].trim()
                    );

            LocalDate checkOut =
                    LocalDate.parse(
                            parts[4].trim()
                    );

            // -----------------------------------------
            // BASIC VALIDATION
            // -----------------------------------------

            if (budget <= 0) {

                sendMessage(
                        chatId,
                        "❌ Budget must be greater than 0."
                );

                return;
            }

            if (guests <= 0) {

                sendMessage(
                        chatId,
                        "❌ Number of guests must be greater than 0."
                );

                return;
            }

            if (!checkOut.isAfter(checkIn)) {

                sendMessage(
                        chatId,

                        "❌ Checkout date must be after check-in date."
                );

                return;
            }

            // -----------------------------------------
            // SEARCH MESSAGE
            // -----------------------------------------

            sendMessage(
                    chatId,

                    "🔎 Searching actual properties...\n\n"
                    + "📍 " + location
                    + "\n💰 Budget: ₹" + budget
                    + " total stay"
                    + "\n👥 Guests: " + guests
                    + "\n📅 Check-in: " + checkIn
                    + "\n📅 Check-out: " + checkOut
            );

            // -----------------------------------------
            // CALL API
            // -----------------------------------------

            List<RealProperty> properties =
                    propertyApiService.searchProperties(
                            location,
                            budget,
                            guests,
                            checkIn,
                            checkOut
                    );

            // -----------------------------------------
            // NO RESULTS
            // -----------------------------------------

            if (properties.isEmpty()) {

                sendMessage(
                        chatId,

                        "😔 No matching properties found.\n\n"
                        + "Try increasing your total budget "
                        + "or changing the dates."
                );

                return;
            }

            // -----------------------------------------
            // CREATE RESPONSE
            // -----------------------------------------

            StringBuilder response =
                    new StringBuilder();

            response.append(
                    "🏠 Actual Property Suggestions\n\n"
            );

            int count = 1;

            for (RealProperty property :
                    properties) {

                if (count > 5) {
                    break;
                }

                response.append(
                        count
                )
                .append(". ")
                .append(
                        property.getName()
                )
                .append("\n");

                response.append(
                        "📍 "
                )
                .append(
                        property.getLocation()
                )
                .append("\n");

                response.append(
                        "💰 ₹"
                )
                .append(
                        String.format(
                                "%.2f",
                                property.getPrice()
                        )
                )
                .append(
                        " total stay\n"
                );

                response.append(
                        "⭐ "
                )
                .append(
                        property.getRating()
                )
                .append("\n");

                response.append(
                        "🏠 "
                )
                .append(
                        property.getRoomType()
                )
                .append("\n");

                if (property
                        .getBookingUrl()
                        != null
                        &&
                        !property
                                .getBookingUrl()
                                .isBlank()) {

                    response.append(
                            "🔗 "
                    )
                    .append(
                            property.getBookingUrl()
                    )
                    .append("\n");
                }

                response.append("\n");

                count++;
            }

            // -----------------------------------------
            // SEND RESULTS
            // -----------------------------------------

            sendMessage(
                    chatId,
                    response.toString()
            );
        }

        catch (NumberFormatException e) {

            sendMessage(
                    chatId,

                    "❌ Invalid budget or guest number.\n\n"
                    + "Example:\n"
                    + "Nagpur,3000,3,2026-10-15,2026-10-17"
            );
        }

        catch (java.time.format.DateTimeParseException e) {

            sendMessage(
                    chatId,

                    "❌ Invalid date format. Please use YYYY-MM-DD.\n\n"
                    + "Example:\n"
                    + "Nagpur,3000,3,2026-10-15,2026-10-17"
            );
        }

        catch (Exception e) {

            e.printStackTrace();

            sendMessage(
                    chatId,

                    "❌ Unable to search properties.\n\n"
                    + "Please check your location and dates "
                    + "and try again."
            );
        }
    }

    private void sendMessage(
            long chatId,
            String text) {

        SendMessage message =
                SendMessage.builder()
                        .chatId(chatId)
                        .text(text)
                        .build();

        try {

            telegramClient.execute(
                    message
            );

        }

        catch (Exception e) {

            e.printStackTrace();
        }
    }
}