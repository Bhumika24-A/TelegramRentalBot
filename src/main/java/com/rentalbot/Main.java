package com.rentalbot;

import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

public class Main {

    public static void main(String[] args) {

        try {

            Properties properties = new Properties();

            // Attempt 1: Load from classpath (e.g. target/classes/config.properties)
            InputStream input =
                    Main.class
                            .getClassLoader()
                            .getResourceAsStream("config.properties");

            // Attempt 2: Load from working directory if not found on classpath
            if (input == null) {
                File localConfig = new File("config.properties");
                if (localConfig.exists()) {
                    input = new FileInputStream(localConfig);
                }
            }

            // Attempt 3: Load from src/main/resources/config.properties
            if (input == null) {
                File srcConfig = new File("src/main/resources/config.properties");
                if (srcConfig.exists()) {
                    input = new FileInputStream(srcConfig);
                }
            }

            if (input != null) {
                try (InputStream stream = input) {
                    properties.load(stream);
                }
            }

            // Read properties with environment variable fallbacks
            String botToken = properties.getProperty("bot.token");
            if (botToken == null || botToken.isBlank()) {
                botToken = System.getenv("BOT_TOKEN");
            }

            String rapidApiKey = properties.getProperty("rapidapi.key");
            if (rapidApiKey == null || rapidApiKey.isBlank()) {
                rapidApiKey = System.getenv("RAPIDAPI_KEY");
            }

            String rapidApiHost = properties.getProperty("rapidapi.host");
            if (rapidApiHost == null || rapidApiHost.isBlank()) {
                rapidApiHost = System.getenv("RAPIDAPI_HOST");
            }
            if (rapidApiHost == null || rapidApiHost.isBlank()) {
                rapidApiHost = "booking-com15.p.rapidapi.com";
            }

            if (botToken == null || botToken.isBlank()) {
                throw new IllegalStateException(
                        "Telegram bot token missing. Please configure 'bot.token' in config.properties "
                        + "(refer to config.properties.example) or set the BOT_TOKEN environment variable."
                );
            }

            if (rapidApiKey == null || rapidApiKey.isBlank()) {
                throw new IllegalStateException(
                        "RapidAPI key missing. Please configure 'rapidapi.key' in config.properties "
                        + "(refer to config.properties.example) or set the RAPIDAPI_KEY environment variable."
                );
            }

            /*
             * Telegram client
             */
            TelegramClient telegramClient =
                    new OkHttpTelegramClient(
                            botToken
                    );

            /*
             * Real property API
             */
            PropertyApiService propertyApiService =
                    new PropertyApiService(
                            rapidApiKey,
                            rapidApiHost
                    );

            /*
             * Rental bot
             */
            RentalBot bot =
                    new RentalBot(
                            telegramClient,
                            propertyApiService
                    );

            /*
             * Start Telegram application
             */
            TelegramBotsLongPollingApplication application =
                    new TelegramBotsLongPollingApplication();

            application.registerBot(
                    botToken,
                    bot
            );

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    System.out.println("Stopping Telegram bot...");
                    application.close();
                } catch (Exception ignored) {
                }
            }));

            System.out.println(
                    "======================================"
            );
            System.out.println(
                    " REAL PROPERTY TELEGRAM BOT STARTED"
            );
            System.out.println(
                    "======================================"
            );
            System.out.println(
                    "Open Telegram and send /start"
            );

        } catch (Exception e) {

            System.out.println(
                    "BOT FAILED TO START"
            );

            e.printStackTrace();
        }
    }
}