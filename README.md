# RequestBin Clone - API Request Logger

A Spring Boot application for capturing and inspecting HTTP requests in real-time. Perfect for debugging webhooks, testing API integrations, and monitoring HTTP traffic.

## Features

✅ Create unique bins to capture HTTP requests  
✅ Support for all HTTP methods (GET, POST, PUT, DELETE, PATCH, etc.)  
✅ Captures headers, body, query parameters, and IP addresses  
✅ Real-time auto-refresh UI  
✅ Persistent bins with customizable expiry (1-168 hours)  
✅ Rate limiting to prevent abuse  
✅ Automatic cleanup of expired bins  
✅ Search and filter functionality  
✅ Pagination for large request lists  
✅ Toast notifications for better UX  
✅ Dark mode UI

## Tech Stack

**Backend:**
- Java 17
- Spring Boot 3.2.x
- Spring Data JPA
- H2 Database (development)
- Bucket4j (rate limiting)
- Lombok

**Frontend:**
- HTML5, CSS3, JavaScript
- LocalStorage for persistence
- Fetch API for REST calls

## Getting Started

### Prerequisites
- Java 17 or higher
- Maven 3.6+

### Installation

1. Clone the repository:
```bash
git clone https://github.com/shammi2k/requestbin-clone.git
cd requestbin-clone
```

2. Run the application:
```bash
./mvnw spring-boot:run
```

3. Open your browser:
```
http://localhost:8080
```

### Usage

1. **Create a Bin:**
    - Enter a name for your bin
    - Set expiry hours (1-168)
    - Set max requests (10-10,000)
    - Click "Create Bin"

2. **Send Requests:**
    - Copy the generated bin URL
    - Send any HTTP request to that URL
    - View captured requests in real-time

3. **Inspect Requests:**
    - Click on any request to expand details
    - View headers, body, query params
    - Filter by HTTP method
    - Navigate through pages

### API Endpoints

#### Bin Management
```
POST   /api/bins              - Create new bin
GET    /api/bins/{id}         - Get bin details
GET    /api/bins/{id}/details - Get bin with all requests
DELETE /api/bins/{id}         - Delete bin
```

#### Request Capture
```
ANY    /b/{uniqueUrl}         - Capture request (all HTTP methods)
```

### Configuration

Edit `src/main/resources/application.properties`:
```properties
# Server port
server.port=8080

# Default bin expiry (hours)
app.bin.default-expiry-hours=24

# Max requests per bin
app.bin.max-requests=1000
```

### Rate Limits

- **Bin Creation:** 10 bins per hour per IP
- **Request Capture:** 60 requests per minute per bin

## Project Structure
```
src/
├── main/
│   ├── java/com/devtools/requestbin/
│   │   ├── config/          # Configuration classes
│   │   ├── controller/      # REST controllers
│   │   ├── dto/             # Data Transfer Objects
│   │   ├── entity/          # JPA entities
│   │   ├── exception/       # Custom exceptions
│   │   ├── repository/      # Spring Data repositories
│   │   └── service/         # Business logic
│   └── resources/
│       ├── static/          # Frontend (HTML/CSS/JS)
│       └── application.properties
└── test/                    # Unit and integration tests
```