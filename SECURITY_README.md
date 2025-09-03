# Security Implementation - JWT Authentication

This task manager now includes JWT-based authentication and authorization.

## Features Implemented

### 1. JWT Authentication
- Token-based authentication using JSON Web Tokens
- Secure token generation and validation
- 24-hour token expiration (configurable)

### 2. User Management
- User registration and login endpoints
- Password encryption using BCrypt
- Role-based access control (USER, ADMIN)

### 3. GraphQL Security
- Protected mutations requiring authentication
- Method-level security with `@PreAuthorize`
- User context available in GraphQL operations

## API Endpoints

### Authentication Endpoints (REST)

#### Register a new user
```bash
POST /auth/register
Content-Type: application/json

{
  "username": "testuser",
  "email": "test@example.com", 
  "firstName": "Test",
  "lastName": "User",
  "password": "password123"
}
```

#### Login
```bash
POST /auth/login
Content-Type: application/json

{
  "username": "testuser",
  "password": "password123"
}
```

Response:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "username": "testuser",
  "email": "test@example.com",
  "role": "USER"
}
```

### GraphQL Endpoints

All GraphQL mutations now require authentication. Include the JWT token in the Authorization header:

```bash
POST /graphql
Authorization: Bearer <your-jwt-token>
Content-Type: application/json

{
  "query": "mutation { createTask(input: { title: \"My Task\" }) { id title } }"
}
```

## Permission Levels

### USER Role
- Can create, update, and delete tasks
- Can assign/unassign users to/from tasks
- Can query all data

### ADMIN Role  
- All USER permissions
- Can create, update, and delete users
- Can manage user roles

## Default Test Users

When running in `local` or `dev` profile, these users are automatically created:

- **Admin User**
  - Username: `admin`
  - Password: `admin123`
  - Role: `ADMIN`

- **Test User**
  - Username: `user`
  - Password: `user123`
  - Role: `USER`

## Testing

Use the provided test script to verify security:

```bash
./test-security.sh
```

This script tests:
1. User registration
2. User login 
3. Unauthorized access (should fail)
4. Authorized GraphQL operations (should succeed)
5. Querying data with authentication

## Configuration

JWT settings can be configured in `application.properties`:

```properties
# JWT Configuration
jwt.secret=myVerySecretKeyThatShouldBeAtLeast256BitsLong1234567890
jwt.expiration=86400000  # 24 hours in milliseconds
```

## Security Notes

- Tokens expire after 24 hours by default
- Passwords are encrypted using BCrypt
- All mutations require authentication
- Queries require authentication (can be made public if needed)
- H2 console and auth endpoints are publicly accessible for development

## Next Steps

For production deployment:
1. Use environment variables for JWT secret
2. Configure HTTPS
3. Set up proper database credentials
4. Consider implementing refresh tokens
5. Add rate limiting for auth endpoints
