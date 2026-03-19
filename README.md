# Axacrate WMS – Backend

Backend service for the Axacrate RFID-Based Warehouse Management System.

This Spring Boot application provides REST APIs for managing:

- RFID tag registration and validation
- Inventory items
- Warehouse zones
- Movement tracking and logging
- Geofencing validations
- Alerts and reporting

The backend serves both:
- React Web Portal (Admin Interface)
- React Native Mobile Application

---

## Tech Stack

- Java 17+
- Spring Boot
- Spring Data JPA
- PostgreSQL
- Maven

---

## System Architecture

ESP32 RFID Readers → REST API → Service Layer → PostgreSQL  
Web Portal & Mobile App consume the same REST endpoints.

---

## Core Modules

- controller → REST endpoints
- service → Business logic & validations
- repository → Database access layer
- entity → JPA entities
- dto → Request/Response models

---

## Setup Instructions

### Clone Repository

git clone https://github.com/pulinduDeSilva/Axacrate-Technologies.git
cd axacrate-wms-backend

---

### Configure PostgreSQL

Create database:

CREATE DATABASE rfid_wms;

Update `application.properties`:

spring.datasource.url=jdbc:postgresql://localhost:5432/rfid_wms  
spring.datasource.username=your_username  
spring.datasource.password=your_password

---

### Run Application

./mvnw spring-boot:run

Backend runs at:

http://localhost:8080

---

## API Usage

Example endpoints:

POST /api/rfid/read  
POST /api/rfid/write-scan  
GET /api/items  
GET /api/zones  
GET /api/movements  
GET /api/alerts

---

## Contributors

- Isith Bhanuka
- Sheshan Thisal
- Aatif Noor
- Pulindu De Silva
- Daniru Senarathne
- Ishan Ahintha
