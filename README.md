# Customer Management System - Backend

A Spring Boot REST API for managing customers.

## Technologies Used

- **Java 8**
- **Spring Boot 2.7.11**
- **Spring Data JPA**
- **MariaDB**
- **Maven**
- **Spring Boot Validation**

## Features

- ✅ CRUD operations for customers
- ✅ Family member relationships
- ✅ Multiple addresses and mobile numbers per customer
- ✅ Master data for cities and countries

## Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/cms/
│   │       ├── Application.java
│   │       ├── controllers/
│   │       ├── dto/
│   │       ├── entity/
│   │       ├── exception/
│   │       ├── repository/
│   │       └── service/
│   └── resources/
│       ├── application.properties
└── test/
    └── java/
        └── com/cms/
```
The API will be available at `http://localhost:8080`

## API Endpoints

### Customer Operations
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/customers` | Get all customers with pagination |
| GET | `/api/customers/{id}` | Get customer by ID |
| POST | `/api/customers` | Create new customer |
| PUT | `/api/customers/{id}` | Update customer |

### Master Data
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/customers/cities` | Get all cities |
| GET | `/api/customers/countries` | Get all countries |

## Request/Response Examples

### Create Customer
```json
POST /api/customers
[{
    "name": "Liam Dias",
    "dateOfBirth": "1992-06-15",
    "nicNumber": "199212345601",
    "mobileNumbers": ["0771234567"],
    "addresses": [
      {
        "addressLine1": "12 Park Lane",
        "addressLine2": "Suite 5",
        "cityId": 3,
        "cityName": "Chennai",
        "countryName": "India"
      }
    ]
  },
{
    "name": "Aiden James",
    "dateOfBirth": "1990-04-12",
    "nicNumber": "199012345015",
    "mobileNumbers": ["0709978775"],
    "addresses": [
      {
        "addressLine1": "123 Mango Avenue",
        "addressLine2": "House 5",
        "cityId": 1,
        "cityName": "Colombo",
        "countryName": "Sri Lanka"
      }
    ],
    "familyMembers": [
      {
        "familyMemberName": "Amelia Fonseka",
        "nicNumber": "199612345614",
        "dateOfBirth": "1996-09-01"
      }
    ]
  }

```



