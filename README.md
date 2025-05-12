# 🏨 Hotel Booking System API

A Spring Boot-based RESTful API designed to manage hotel listings, room availability, amenities, bookings, reviews, and dynamic pricing for a modern hotel reservation platform.

## 📚 Project Overview

This project is the backend system for a hotel booking application that includes the following features:

- User registration and role-based access
- Hotel and room management by hotel owners
- Room availability tracking and booking
- Payment processing
- Dynamic pricing with configurable rules
- Amenity and image management
- User reviews and ratings

## 🧩 Database Schema

The system is built around a normalized relational database with the following core tables:

- `users`: Stores user data including roles like admin, guest, or hotel owner.
- `hotel`: Contains information about registered hotels.
- `room`: Represents rooms within hotels with pricing and capacity details.
- `room_availability`: Tracks daily availability of rooms.
- `booking`: Handles user bookings for rooms.
- `payment`: Captures details of booking payments.
- `amenity`: Lists amenities available in rooms or hotels.
- `room_amenity`: Mapping between rooms and their amenities.
- `review`: Stores user-submitted reviews for hotels.
- `image`: Manages image URLs for hotels, rooms, or amenities.
- `pricing_rule`: Allows dynamic pricing rules based on date range or other criteria.

## 📦 Tech Stack

- **Java 17+**
- **Spring Boot**
- **Spring Data JPA**
- **PostgreSQL/MySQL** (Database agnostic structure)
- **Lombok**
- **Gradle**
- **UUIDs** for all primary keys

## 🚀 Getting Started

### Prerequisites

- Java 17+
- Maven or Gradle
- PostgreSQL or MySQL running
- Docker (optional, for containerization)

### Clone the Repository

```bash
git clone https://github.com/your-username/hotel-booking-api.git
cd hotel-booking-api
```
