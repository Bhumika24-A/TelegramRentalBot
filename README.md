# 🏠 Real Property Rental Bot for Telegram

A Java-based Telegram assistant that searches real accommodation listings (hotels, apartments, and rentals) using the Booking.com API via RapidAPI. Users can enter their destination, maximum budget, party size, and stay dates directly in Telegram to receive sorted property recommendations with live pricing, review scores, room specifications, and direct booking links.

---

## ✨ Features

- **Interactive Telegram Bot**: Built on the modern Telegram Bots Long Polling API (`telegrambots` 10.3.0).
- **Live Booking Data**: Queries live hotel and rental property data via RapidAPI's Booking.com API.
- **Smart Filtering**: Automatically filters listings to match user-defined budgets and sorts them by price.
- **Detailed Listings**: Displays property name, location, total stay cost in INR, review ratings, room configuration, and reservation links.
- **Input Validation**: Robust parsing and user-friendly error messages for date formats (`YYYY-MM-DD`), positive budgets, and valid stay durations.
- **Security First**: Secrets (`bot.token`, `rapidapi.key`) are decoupled from source control with `.gitignore` exclusion and flexible support for both `config.properties` and environment variables.

---

## 🛠️ Tech Stack & Architecture

- **Language**: Java 21+ (Compatible with JDK 21 and JDK 25)
- **Build Tool**: Apache Maven 3.8+
- **Telegram Library**: [TelegramBots](https://github.com/rubenlagus/TelegramBots) (`telegrambots-longpolling`, `telegrambots-client` 10.3.0)
- **HTTP Client**: Java 11 `java.net.http.HttpClient`
- **JSON Processing**: Jackson Databind 2.17.2
- **Testing**: JUnit 5 (Jupiter)

### Project Layout

```
TelegramRentalBot/
├── pom.xml                               # Maven project definition and dependencies
├── config.properties.example             # Configuration template with placeholders
├── .gitignore                            # Excludes secrets, target, and IDE metadata
├── README.md                             # Documentation
└── src/
    ├── main/
    │   ├── java/com/rentalbot/
    │   │   ├── Main.java                 # Entry point, config loader, bot lifecycle
    │   │   ├── RentalBot.java            # Telegram update consumer, validation & messaging
    │   │   ├── PropertyApiService.java   # RapidAPI client & JSON response parser
    │   │   └── RealProperty.java         # Rental property data model
    │   └── resources/
    │       ├── config.properties.example # Classpath configuration template
    │       └── config.properties         # Local config (excluded from Git)
    └── test/
        └── java/com/rentalbot/
            ├── RealPropertyTest.java     # Model tests
            └── SearchInputValidationTest.java # Validation & date parsing tests
```

---

## 📋 Prerequisites

Before running the application, make sure you have:

1. **Java Development Kit (JDK)**: JDK 21 or higher installed (`java -version`).
2. **Apache Maven**: Maven 3.8 or higher installed (`mvn -version`).
3. **Telegram Bot Token**:
   - Talk to [@BotFather](https://t.me/BotFather) on Telegram.
   - Run `/newbot` to create your bot and obtain the API bot token.
4. **RapidAPI Account**:
   - Subscribe to the [Booking.com API on RapidAPI](https://rapidapi.com/).
   - Copy your `x-rapidapi-key`.

---

## 🚀 Quick Start & Setup

### 1. Clone the Repository

```bash
git clone https://github.com/Bhumika24-A/TelegramRentalBot.git
cd TelegramRentalBot
```

### 2. Configure Credentials

Copy the configuration template to `src/main/resources/config.properties`:

```bash
# On Windows PowerShell:
Copy-Item config.properties.example src/main/resources/config.properties

# On Linux / macOS:
cp config.properties.example src/main/resources/config.properties
```

Open `src/main/resources/config.properties` and fill in your credentials:

```properties
bot.username=YourTelegramBotUsername
bot.token=YOUR_TELEGRAM_BOT_TOKEN_HERE

rapidapi.key=YOUR_RAPIDAPI_KEY_HERE
rapidapi.host=booking-com15.p.rapidapi.com
```

> **Alternative (Environment Variables)**:  
> You can also supply credentials via environment variables without creating a file:
> ```bash
> export BOT_TOKEN="your_bot_token"
> export RAPIDAPI_KEY="your_rapidapi_key"
> export RAPIDAPI_HOST="booking-com15.p.rapidapi.com"
> ```

### 3. Build the Project

Run tests and compile:

```bash
mvn clean test
```

### 4. Run the Bot

Start the bot using Maven:

```bash
mvn exec:java
```

Once started, the console will display:
```
======================================
 REAL PROPERTY TELEGRAM BOT STARTED
======================================
Open Telegram and send /start
```

---

## 💬 Using the Bot

1. Open Telegram and search for your bot username.
2. Send `/start` to receive the welcome prompt.
3. Send `/search` for instructions and query format.
4. Submit your search parameters as a comma-separated line:
   ```
   <location>,<budget>,<guests>,<checkin>,<checkout>
   ```

### Search Examples

- Search for accommodation in **Nagpur** for **3 guests**, budget **₹3000**, from **2026-10-15** to **2026-10-17**:
  ```
  Nagpur,3000,3,2026-10-15,2026-10-17
  ```
- Search for a stay in **Goa** for **2 guests**, budget **₹6000**, from **2026-11-01** to **2026-11-05**:
  ```
  Goa,6000,2,2026-11-01,2026-11-05
  ```

The bot will reply with up to 5 matching accommodations within your budget, including names, ratings, total prices, room descriptions, and direct booking links.

---

## 🧪 Running Automated Tests

Run the test suite using:

```bash
mvn test
```

Unit tests cover:
- Property model instantiation and accessors.
- Format parsing for search commands.
- Budget and guest count numerical boundaries.
- Date ordering validation (`checkOut` after `checkIn`).
- Date format checks (`YYYY-MM-DD`).

---

## 🔒 Security Best Practices

- `config.properties` is explicitly listed in `.gitignore` to prevent API keys and bot tokens from being committed.
- Never commit actual tokens or credentials to version control.
- If tokens are accidentally exposed, revoke and regenerate them immediately via [@BotFather](https://t.me/BotFather) and the RapidAPI dashboard.

---

## 📄 License

This project is open-source and available under the [MIT License](LICENSE).
