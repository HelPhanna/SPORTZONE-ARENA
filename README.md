# SportZone Arena - Sport Field Booking System

A modern Spring Boot web application for booking sports fields (Football, Futsal, Badminton, Tennis, Basketball, and Volleyball) with integrated Bakong KHQR payments.

---

## Features
- **Online Field Booking**: Browse venues, check real-time slot availability, and book slots.
- **Bakong KHQR Payments**: Real-time Cambodian QR payment processing and verification via National Bank of Cambodia (NBC) Bakong API.
- **Admin Dashboard**: Manage fields, categories, pricing, bookings, payments, and users.
- **Role-based Authentication**: Spring Security with Admin and Customer roles.

---

## Tech Stack
- **Backend**: Java 17, Spring Boot 3.3.5, Spring Security, Spring Data JPA, Flyway
- **Database**: MySQL 8.x
- **Frontend**: Thymeleaf, Bootstrap, HTML5/CSS3/JavaScript
- **Payment Gateway**: Bakong KHQR Open API

---

## Quick Setup & Installation

### 1. Clone the repository
```bash
git clone https://github.com/HelPhanna/SPORTZONE-ARENA.git
cd SPORTZONE-ARENA
```

### 2. Configure Environment Variables
Copy `.env.example` to `.env`:
```bash
cp .env.example .env
```
Open `.env` and fill in your database credentials:
```env
DB_URL=jdbc:mysql://localhost:3309/sport_booking
DB_USERNAME=root
DB_PASSWORD=your_mysql_password
BAKONG_TOKEN=your_bakong_token_here
```

### 3. Run the Application
```bash
./mvnw spring-boot:run
```
Visit `http://localhost:8080` in your browser.

