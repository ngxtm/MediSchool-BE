# MediSchool Backend

A comprehensive Spring Boot application for managing school health services, including vaccination tracking, health checkups, medication requests, and student health records.

## 🚀 Features

### Core Functionality

- **Student Management** - Complete student profile and class management
- **Vaccination System** - Track vaccination events, consents, and history
- **Health Checkups** - Manage health checkup events and results
- **Medication Tracking** - Handle medication requests and prescriptions
- **User Authentication** - JWT-based authentication with role management
- **Email Notifications** - Automated email system for health events
- **PDF Reports** - Generate vaccination history and health reports
- **Excel Integration** - Import/export functionality for bulk operations

### Advanced Features

- **Async Email Processing** - Bulk email notifications with retry mechanism
- **Activity Logging** - Comprehensive audit trail for all operations
- **Multi-language Support** - Internationalization ready
- **API Documentation** - Complete Swagger/OpenAPI documentation
- **Security** - Role-based access control and data protection

## 🛠 Tech Stack

- **Framework**: Spring Boot 3.5.0
- **Language**: Java 21
- **Database**: PostgreSQL (Supabase)
- **Security**: Spring Security + JWT
- **Documentation**: Swagger/OpenAPI 3
- **Email**: Spring Mail with Gmail SMTP + Supabase Mail Support
- **PDF Generation**: iText 7
- **Excel Processing**: Apache POI
- **Build Tool**: Maven

## 📋 Prerequisites

- Java 21 or higher
- Maven 3.6+
- PostgreSQL database (or Supabase account)
- Gmail account with App Password (for email features)

## ⚡ Quick Start

### 1. Clone the Repository

```bash
git clone <repository-url>
cd medischool-backend
```

### 2. Environment Setup

Create environment variables or update `application.properties`:

```bash
# Database (Supabase)
SUPABASE_JWT_SECRET=your-supabase-jwt-secret
SUPABASE_API_KEY=your-supabase-api-key
SUPABASE_API_KEY_ADMIN=your-supabase-admin-key

# Email Configuration
SPRING_MAIL_USERNAME=your-gmail@gmail.com
SPRING_MAIL_PASSWORD=your-16-character-app-password

# Frontend URL
APP_FRONTEND_URL=http://localhost:5173
```

### 3. Run the Application

```bash
./mvnw spring-boot:run
```

The application will start on `http://localhost:8080`

### 4. Access API Documentation

Visit: `http://localhost:8080/swagger-ui/index.html`

## 📚 API Endpoints

### Authentication

- `POST /api/auth/login` - User login
- `POST /api/auth/register` - User registration
- `POST /api/auth/refresh` - Refresh JWT token

### Student Management

- `GET /api/students` - List all students
- `POST /api/students` - Create new student
- `GET /api/students/{id}` - Get student details
- `PUT /api/students/{id}` - Update student

### Vaccination System

- `GET /api/vaccines` - List available vaccines
- `POST /api/vaccine-events` - Create vaccination event
- `GET /api/vaccine-consents` - Manage vaccination consents
- `GET /api/vaccination-history` - View vaccination history
- `GET /api/vaccination-history/event/{eventId}/pdf` - Export event PDF
- `GET /api/vaccination-history/student/{studentId}/pdf` - Export student PDF

### Health Checkups

- `GET /api/health-checkup` - List checkup events
- `POST /api/checkup-consents` - Manage checkup consents
- `GET /api/checkup-results` - View checkup results

### Email System

- `POST /api/vaccine-events/{eventId}/send-email-notifications` - Send bulk notifications

## 🔧 Configuration

### Database Configuration

The application uses PostgreSQL via Supabase. Update the connection string in `application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://your-db-host:5432/your-database
```

### Email Configuration

For Gmail SMTP, you need to:

1. Enable 2-Step Verification
2. Generate an App Password
3. Set environment variables

See [EMAIL_SETUP_GUIDE.md](EMAIL_SETUP_GUIDE.md) for detailed instructions.

### Security Configuration

JWT tokens expire after 30 minutes by default. Modify in `application.properties`:

```properties
app.jwt.expiration.minutes=30
```

## 📖 Documentation

- [Email Setup Guide](EMAIL_SETUP_GUIDE.md) - Configure Gmail SMTP
- [Email Usage Guide](EMAIL_USAGE_GUIDE.md) - Using the email system
- [PDF Export Guide](PDF_EXPORT_GUIDE.md) - PDF generation features

## 🏗 Project Structure

```
src/main/java/com/medischool/backend/
├── annotation/          # Custom annotations
├── aspect/             # AOP aspects for logging
├── config/             # Configuration classes
├── controller/         # REST controllers
│   ├── checkup/       # Health checkup endpoints
│   ├── healthevent/   # Health event endpoints
│   └── vaccination/   # Vaccination endpoints
├── dto/               # Data Transfer Objects
├── model/             # JPA entities
├── repository/        # Data access layer
├── security/          # Security configuration
├── service/           # Business logic
└── util/              # Utility classes
```

## 🚀 Development

### Running Tests

```bash
./mvnw test
```

### Building for Production

```bash
./mvnw clean package
```

### Docker Support

```bash
docker build -t medischool-backend .
docker run -p 8080:8080 medischool-backend
```

## 📊 Features in Detail

### Vaccination Management

- Create vaccination events for specific classes
- Manage parent consents with email notifications
- Track vaccination history with detailed records
- Generate PDF reports for events and individual students
- Support for multiple vaccine types and doses

### Health Checkup System

- Schedule health checkup events
- Collect basic health information
- Record checkup results with categories
- Generate comprehensive health reports

### Email System

- Asynchronous bulk email processing
- Connection pooling for performance
- Retry mechanism for failed emails
- Customizable email templates
- Support for HTML and plain text emails

### Security Features

- JWT-based authentication
- Role-based access control (Admin, Teacher, Parent)
- Activity logging for audit trails
- Secure password handling
- CORS configuration for frontend integration

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📝 License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details.

## 🆘 Support

For support and questions:

- Check the documentation files in the repository
- Review the API documentation at `/swagger-ui/index.html`
- Create an issue in the repository

## 🔄 Version History

- **v0.0.1-SNAPSHOT** - Initial release with core functionality
  - Student and class management
  - Vaccination tracking system
  - Health checkup management
  - Email notification system
  - PDF report generation
  - Excel import/export capabilities
