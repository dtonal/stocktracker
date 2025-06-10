# My Stock Tracker

## Project Description

"My Stock Tracker" is a web application designed for managing personal stock portfolios. Users can record their purchased securities, automatically retrieve their current market prices, and track the performance of their portfolios in real-time (or daily updated). The goal of this project is to gain a comprehensive understanding of the **Spring Framework** by implementing many of its core aspects.

---

## Features to Implement

We will iteratively develop the following features to learn and apply various modules of the Spring Framework:

### 1. Basic Structure & Persistence (Spring Data JPA, H2 Database)
* **Spring Boot Project Initialization:** Setting up the foundational project structure.
* **Data Modeling:** Defining entities for `User`, `Portfolio`, `StockTransaction` (buy/sell of a stock), `Stock` (information about the stock itself, e.g., symbol, name), and `HistoricalPrice` (daily market prices).
* **Repository Layer:** Implementing the database access layer using **Spring Data JPA**.

### 2. User Management & Security (Spring Security)
* **Registration:** Users can register with a username and password. Passwords will be securely hashed.
* **Login/Authentication:** Users can log in and be authenticated.
* **Role-Based Authorization:** Simple distinction between `USER` and `ADMIN` roles.
* **Access Control:** Ensuring users can only access and manage their own portfolios.

### 3. Portfolio & Transaction Management (Spring MVC, Validation)
* **Portfolio Creation/Editing/Deletion:** Users can create and manage multiple portfolios.
* **Adding/Removing Stocks:** Adding a stock transaction (buy with quantity, purchase price, date) to a portfolio.
* **Validation:** Ensuring user inputs are correct and valid (e.g., positive quantity, valid stock symbol).
* **RESTful API:** Providing endpoints for all CRUD (Create, Read, Update, Delete) operations on portfolios and transactions.

### 4. Stock Price Retrieval & Data Processing (Scheduled Tasks, REST Client)
* **External Stock API Integration:** Connecting to a free API (e.g., Alpha Vantage, Finnhub) to retrieve stock data.
* **Scheduled Price Retrieval:** Automatic, time-based (e.g., daily) fetching of current and historical prices for all stocks tracked in the system. We will use **Spring's `@Scheduled` annotation** for this.
* **Price Data Storage:** Persisting the retrieved stock prices in the database.

### 5. Portfolio Analytics & Reporting
* **Current Portfolio Value:** Calculating the total value of a portfolio based on current stock prices.
* **Profit/Loss:** Calculating realized and unrealized profit/loss per stock and for the entire portfolio.
* **Performance History:** Providing data to display portfolio development over time.
* **Aggregated Data:** Offering endpoints for summary reports (e.g., top-performing stocks).

### 6. Additional Spring Aspects & Best Practices
* **Transaction Management:** Ensuring data consistency for complex operations (e.g., buying a stock) using **`@Transactional`**.
* **Global Exception Handling:** Robust error handling for the REST API using **`@ControllerAdvice`**.
* **DTOs (Data Transfer Objects):** Clean separation between database entities and API exposure.
* **Testing:** Implementing unit and integration tests for all layers of the application.
* **Spring Boot Actuator:** Monitoring and managing the running application.
* **AOP (Aspect-Oriented Programming):** Optional implementation for logging service calls or performance measurements.
* **Caching (Optional):** Caching frequently accessed data to save API limits and improve performance.
* **Profiles:** Configuration for different environments (development, production) using `application.properties`.

---