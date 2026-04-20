# Getting Started Guide

<cite>
**Referenced Files in This Document**
- [README.md](file://README.md)
- [pom.xml](file://src/Backend/pom.xml)
- [application.properties](file://src/Backend/src/main/resources/application.properties)
- [MONGODB_SETUP_GUIDE.md](file://src/Backend/MONGODB_SETUP_GUIDE.md)
- [run-dev.bat](file://src/Backend/run-dev.bat)
- [.mvn/wrapper/maven-wrapper.properties](file://src/Backend/.mvn/wrapper/maven-wrapper.properties)
- [BackendApplication.java](file://src/Backend/src/main/java/com/shoppeclone/backend/BackendApplication.java)
- [DataInitializer.java](file://src/Backend/src/main/java/com/shoppeclone/backend/common/config/DataInitializer.java)
- [index.html](file://src/Backend/src/main/resources/static/index.html)
- [api-tester.html](file://src/Backend/src/main/resources/static/api-tester.html)
- [build_log.txt](file://data_dumps/build_log.txt)
</cite>

## Table of Contents
1. [Introduction](#introduction)
2. [Environment Requirements](#environment-requirements)
3. [Step-by-Step Setup Instructions](#step-by-step-setup-instructions)
4. [Environment Variable Configuration](#environment-variable-configuration)
5. [Initial Application Startup](#initial-application-startup)
6. [Development Workflow](#development-workflow)
7. [Understanding the Basic System Structure](#understanding-the-basic-system-structure)
8. [Troubleshooting Common Setup Issues](#troubleshooting-common-setup-issues)
9. [Verification Steps](#verification-steps)
10. [Conclusion](#conclusion)

## Introduction

Welcome to the Shoppe Clone e-commerce platform! This comprehensive getting started guide will help you set up and run the backend server locally, configure environment variables, and understand the basic system structure. The platform is built with Spring Boot 3.2.3, Java 21, and MongoDB, providing a complete e-commerce solution with authentication, catalog management, shopping cart, orders, payments, promotions, and more.

The system follows a modular architecture with separate modules for authentication, user management, shop management, product catalog, commerce operations, promotions, trust and support, and admin functionality.

## Environment Requirements

Before setting up the e-commerce platform, ensure you have the following prerequisites installed:

### Core Requirements
- **Java 21**: The project requires Java 21 for compilation and execution
- **Maven**: Either Maven 3.9.12 or Maven Wrapper for dependency management
- **MongoDB**: Local MongoDB instance or MongoDB Atlas cloud database
- **Git**: For cloning the repository (recommended)

### Optional Services
- **Gmail Account**: For OTP email functionality using App Passwords
- **Cloudinary Account**: For image upload functionality
- **Google OAuth Credentials**: For Google OAuth login testing
- **Postman**: For API testing and verification

### Hardware Requirements
- Minimum 4GB RAM recommended for smooth development
- Sufficient disk space for MongoDB data storage
- Stable internet connection for downloading dependencies

**Section sources**
- [README.md:118-126](file://README.md#L118-L126)
- [pom.xml:18-21](file://src/Backend/pom.xml#L18-L21)

## Step-by-Step Setup Instructions

Follow these step-by-step instructions to set up the e-commerce platform locally:

### Step 1: Clone the Repository
```bash
git clone https://github.com/your-repository/shoppe-clone.git
cd shoppe-clone
```

### Step 2: Verify Java Installation
```bash
java -version
javac -version
```
Ensure Java 21 is installed and properly configured in your PATH.

### Step 3: Install MongoDB
Choose one of the following options:

**Option A: Local MongoDB Installation**
1. Download MongoDB Community Server from the official website
2. Install and start the MongoDB service
3. Verify installation: `mongo --host localhost --port 27017`

**Option B: MongoDB Atlas (Cloud)**
1. Create a free account at [MongoDB Atlas](https://www.mongodb.com/cloud/atlas/register)
2. Create a new cluster and configure database access
3. Obtain the connection string from the Atlas dashboard

### Step 4: Configure Environment Variables
Navigate to the backend directory and create your environment configuration:

```bash
cd src/Backend
```

Create a `.env` file with your configuration values. The environment variables include:

- **JWT Configuration**: JWT_SECRET, JWT_EXPIRATION, JWT_REFRESH_EXPIRATION
- **Google OAuth**: GOOGLE_CLIENT_ID, GOOGLE_CLIENT_SECRET
- **Email Configuration**: MAIL_USERNAME, MAIL_PASSWORD
- **Cloudinary**: CLOUDINARY_CLOUD_NAME, CLOUDINARY_API_KEY, CLOUDINARY_API_SECRET
- **OTP Configuration**: OTP_EXPIRATION

### Step 5: Configure MongoDB Connection
Update the MongoDB connection in `application.properties`:

```properties
# For Local MongoDB
spring.data.mongodb.uri=mongodb://localhost:27017/web_shoppe

# For MongoDB Atlas
spring.data.mongodb.uri=mongodb+srv://username:password@cluster-url/web_shoppe?retryWrites=true&w=majority
```

### Step 6: Build the Project
```bash
# Using Maven Wrapper (recommended)
./mvnw.cmd clean install

# Or using installed Maven
mvn clean install
```

### Step 7: Run the Application
```bash
# Start the Spring Boot application
./mvnw.cmd spring-boot:run

# Or use the development script
./run-dev.bat
```

**Section sources**
- [README.md:116-177](file://README.md#L116-L177)
- [MONGODB_SETUP_GUIDE.md:22-57](file://src/Backend/MONGODB_SETUP_GUIDE.md#L22-L57)

## Environment Variable Configuration

The e-commerce platform uses environment variables for secure configuration. Here's how to set them up:

### Required Environment Variables

Create a `.env` file in the `src/Backend` directory with the following variables:

```env
# JWT Configuration
JWT_SECRET=YourSuperSecretKeyForJWTTokenGenerationAndValidation123456
JWT_EXPIRATION=900000
JWT_REFRESH_EXPIRATION=604800000

# Google OAuth Configuration
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret

# Email Configuration (Gmail SMTP)
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-16-char-app-password

# OTP Configuration
OTP_EXPIRATION=300000

# Cloudinary Configuration (optional)
CLOUDINARY_CLOUD_NAME=your_cloud_name
CLOUDINARY_API_KEY=your_api_key
CLOUDINARY_API_SECRET=your_api_secret
```

### Configuration Options

The application supports two ways to configure MongoDB:

**Option 1: Properties File Configuration**
Set the MongoDB URI directly in `application.properties`:
```properties
spring.data.mongodb.uri=mongodb://localhost:27017/web_shoppe
```

**Option 2: Environment Variable Override**
Set the MongoDB URI as an environment variable:
```bash
export SPRING_DATA_MONGODB_URI=mongodb://localhost:27017/web_shoppe
```

### Security Considerations

- Use strong, random values for JWT_SECRET (minimum 32 characters)
- Store environment variables securely, never commit them to version control
- Use Gmail App Passwords instead of regular passwords for email functionality
- Keep OAuth credentials in a secure location

**Section sources**
- [README.md:127-148](file://README.md#L127-L148)
- [application.properties:14-16](file://src/Backend/src/main/resources/application.properties#L14-L16)

## Initial Application Startup

After completing the setup, you can start the application using several methods:

### Method 1: Using Maven Wrapper (Recommended)
```bash
cd src/Backend
./mvnw.cmd spring-boot:run
```

### Method 2: Using Development Script
```bash
cd src/Backend
./run-dev.bat
```

### Method 3: Direct Java Command
```bash
cd src/Backend
java -jar target/backend-0.0.1-SNAPSHOT.jar
```

### Expected Startup Output
When the application starts successfully, you should see output similar to:
```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ / /
( ( )\___ | '_ | '_| | '_ \/ _` | \ V /
 \\/ ___)|_| |_|\__|_|_| |_\__, |  | |
            |____/|_____|___/_|___/
 :: Spring Boot ::                (v3.2.3)

... Application started successfully ...
Tomcat initialized with port(s): 8080
Started BackendApplication in X.XXX seconds
```

### Port Configuration
The default application port is 8080. You can change it in `application.properties`:
```properties
server.port=8080
```

**Section sources**
- [README.md:159-171](file://README.md#L159-L171)
- [run-dev.bat:1-12](file://src/Backend/run-dev.bat#L1-L12)

## Development Workflow

Once the application is running, you can explore the e-commerce platform through various interfaces:

### Accessing Demo Pages
The platform provides several static HTML pages for demonstration:

- **Main Landing Page**: `http://localhost:8080/index.html`
- **Login Page**: `http://localhost:8080/login.html`
- **Registration Page**: `http://localhost:8080/register.html`
- **Profile Management**: `http://localhost:8080/profile.html`
- **Shopping Cart**: `http://localhost:8080/cart.html`
- **Product Catalog**: `http://localhost:8080/category.html`
- **API Testing Tool**: `http://localhost:8080/api-tester.html`

### API Base URL
The API base URL is `http://localhost:8080/api`. You can test endpoints using the API tester page or tools like Postman.

### Module-Based Navigation
The platform is organized into functional modules:

- **Authentication Module**: `/api/auth` - Registration, login, refresh tokens
- **User Management**: `/api/user` - Profile, addresses, notifications
- **Shop Management**: `/api/shop` - Shop registration, management
- **Catalog**: `/api/products` - Product management, categories
- **Commerce Operations**: `/api/cart`, `/api/orders` - Shopping cart, orders
- **Promotions**: `/api/vouchers`, `/api/flash-sales` - Vouchers, flash sales
- **Trust & Support**: `/api/reviews`, `/api/refunds`, `/api/disputes` - Reviews, refunds, disputes
- **Admin Functions**: `/api/admin/**` - Administrative operations

### Development Tools
The repository includes useful development tools:

- **Flash Sale Simulator**: `tools/FlashSaleSimulator/` - High-load testing tool
- **PowerShell Scripts**: `tools/*.ps1` - Various development utilities
- **Data Import Tools**: CSV import capabilities for bulk data operations

**Section sources**
- [README.md:228-236](file://README.md#L228-L236)
- [README.md:213-227](file://README.md#L213-L227)
- [index.html:1-50](file://src/Backend/src/main/resources/static/index.html#L1-L50)

## Understanding the Basic System Structure

The e-commerce platform follows a layered architecture with clear separation of concerns:

### Core Architecture Layers

```mermaid
graph TB
subgraph "Presentation Layer"
FE[Frontend HTML/CSS/JS]
API[REST API Controllers]
end
subgraph "Business Logic Layer"
Service[Service Layer]
DTO[DTO Objects]
end
subgraph "Data Access Layer"
Repo[Repositories]
Entity[Domain Entities]
end
subgraph "Infrastructure"
DB[(MongoDB)]
Cloud[Cloudinary]
Mail[SMTP Server]
OAuth[Google OAuth]
end
FE --> API
API --> Service
Service --> Repo
Repo --> Entity
Entity --> DB
Service --> Cloud
Service --> Mail
Service --> OAuth
```

**Diagram sources**
- [README.md:45-56](file://README.md#L45-L56)

### Key Components

**Backend Application Entry Point**
The main application class initializes the Spring Boot application:
- Location: `src/main/java/com/shoppeclone/backend/BackendApplication.java`
- Purpose: Application bootstrap and scheduling configuration

**Data Initialization**
The system automatically seeds essential data during startup:
- Roles (USER, ADMIN, SELLER)
- Default categories
- Payment methods
- Shipping providers
- Admin user promotion

**Security Configuration**
- JWT-based authentication
- Role-based access control
- OAuth2 integration with Google
- Email-based OTP functionality

**Module Organization**
The codebase is organized into feature-based packages:
- `auth/` - Authentication and user management
- `user/` - User profile and preferences
- `shop/` - Shop management and seller operations
- `product/` - Product catalog and inventory
- `cart/` - Shopping cart functionality
- `order/` - Order processing and management
- `promotion/` - Vouchers and flash sales
- `review/` - Product reviews and ratings
- `refund/` - Refund processing
- `dispute/` - Dispute resolution
- `shipping/` - Shipping management
- `payment/` - Payment processing
- `common/` - Shared utilities and configurations

**Section sources**
- [BackendApplication.java:1-14](file://src/Backend/src/main/java/com/shoppeclone/backend/BackendApplication.java#L1-L14)
- [DataInitializer.java:1-50](file://src/Backend/src/main/java/com/shoppeclone/backend/common/config/DataInitializer.java#L1-L50)

## Troubleshooting Common Setup Issues

### Maven Build Issues

**Problem**: Maven cannot resolve parent POM
**Solution**: 
1. Check your internet connection
2. Verify proxy settings in `~/.m2/settings.xml`
3. Try using Maven Wrapper instead: `./mvnw.cmd clean install`

**Problem**: Build fails with dependency resolution errors
**Solution**:
1. Clear Maven cache: `mvn dependency:purge-local-repository`
2. Update dependencies: `mvn -U clean install`
3. Check firewall settings blocking Maven Central

### MongoDB Connection Issues

**Problem**: Cannot connect to MongoDB
**Solution**:
1. Verify MongoDB service is running
2. Check connection string format
3. Ensure database credentials are correct
4. Test connection with: `mongo --host localhost --port 27017`

**Problem**: Authentication failed errors
**Solution**:
1. Verify username/password in connection string
2. Check authSource parameter if using authentication
3. Ensure user has proper database permissions

### Java Version Issues

**Problem**: Compilation errors related to Java version
**Solution**:
1. Verify Java 21 installation: `java -version`
2. Check JAVA_HOME environment variable
3. Ensure Maven is using the correct Java version

### Port Conflicts

**Problem**: Port 8080 already in use
**Solution**:
1. Use the development script which automatically kills conflicting processes
2. Change port in `application.properties`: `server.port=8081`
3. Find and terminate the conflicting process

### Environment Variable Issues

**Problem**: Application cannot find environment variables
**Solution**:
1. Ensure `.env` file is in the correct location (`src/Backend/.env`)
2. Verify variable names match exactly what the application expects
3. Check for typos in variable values

### CORS Issues

**Problem**: Frontend requests blocked due to CORS
**Solution**:
1. Verify CORS configuration in `application.properties`
2. Ensure frontend origin is included in allowed origins
3. Check browser console for specific CORS error messages

**Section sources**
- [build_log.txt:1-21](file://data_dumps/build_log.txt#L1-L21)
- [MONGODB_SETUP_GUIDE.md:159-173](file://src/Backend/MONGODB_SETUP_GUIDE.md#L159-L173)

## Verification Steps

To ensure your installation is working correctly, perform these verification steps:

### 1. Application Startup Verification
1. Start the application using `./mvnw.cmd spring-boot:run`
2. Check for successful startup messages in the console
3. Verify the application is listening on port 8080

### 2. API Endpoint Testing
1. Open `http://localhost:8080/api-tester.html` in your browser
2. Test basic endpoints:
   - `GET /api/products` - Retrieve product catalog
   - `GET /api/categories` - Retrieve categories
   - `GET /api/auth/register` - Test registration endpoint

### 3. Database Connection Verification
1. Test MongoDB connectivity:
   ```bash
   mongo --host localhost --port 27017
   ```
2. Verify database creation and collections
3. Check that default data was seeded

### 4. Frontend Access Verification
1. Navigate to `http://localhost:8080/index.html`
2. Verify all static assets load correctly
3. Test navigation between demo pages
4. Check JavaScript functionality in browser console

### 5. Authentication Flow Testing
1. Register a new user account
2. Login with the registered credentials
3. Verify JWT token generation
4. Test protected endpoints with valid tokens

### 6. Module Functionality Testing
1. Test authentication endpoints
2. Verify user management functionality
3. Check shop registration process
4. Test product catalog browsing
5. Verify shopping cart operations

### 7. Error Handling Verification
1. Test invalid API requests
2. Verify proper error responses
3. Check application logs for errors
4. Ensure graceful error handling

**Section sources**
- [README.md:178-186](file://README.md#L178-L186)
- [api-tester.html:1-50](file://src/Backend/src/main/resources/static/api-tester.html#L1-L50)

## Conclusion

You have successfully set up the Shoppe Clone e-commerce platform! This guide covered all essential aspects of getting started, from environment requirements to initial verification steps.

### What You've Accomplished
- Installed and configured Java 21 and Maven
- Set up MongoDB (local or cloud)
- Configured environment variables and application properties
- Successfully started the Spring Boot application
- Verified API endpoints and frontend functionality
- Tested the basic system structure and module organization

### Next Steps
1. Explore the demo pages to understand user workflows
2. Test the admin panel for administrative functions
3. Experiment with different modules (authentication, catalog, orders)
4. Review the codebase structure to understand implementation patterns
5. Set up your preferred development environment (IDE, debugging tools)

### Additional Resources
- Refer to the comprehensive README for advanced configuration options
- Explore the Swagger documentation for complete API reference
- Check the troubleshooting section for common issues
- Review the module-specific documentation for detailed functionality

The platform is now ready for development and testing. Happy coding, and enjoy building your e-commerce solution!