# Task Manager CLI Client

A simple command-line client to test your Task Manager GraphQL API with JWT authentication.

## Features

- 🔐 **Authentication**: Register and login with JWT tokens
- 📋 **Task Management**: Create, read, update, delete tasks
- 👥 **User Management**: View users and their assigned tasks
- 📊 **Monitoring**: Check application health and metrics
- 🧪 **Testing**: Automated test scenarios
- 🖥️ **Interactive**: Easy-to-use command-line interface

## Quick Start

### Prerequisites

- Python 3.6 or higher
- Task Manager server running on `http://localhost:8080`

### Installation

1. **Set up Python virtual environment (recommended):**
   ```bash
   # Create virtual environment
   python3 -m venv client/venv
   
   # Activate it
   source client/venv/bin/activate
   
   # Install dependencies
   pip install -r requirements.txt
   ```

2. **Alternative: Use the automated setup script:**
   ```bash
   # This will create venv and install dependencies automatically
   ./run_client.sh
   ```

3. **Make sure your Task Manager server is running:**
   ```bash
   # In the task-manager directory
   mvn spring-boot:run
   ```

4. **Run the client:**
   ```bash
   # If using virtual environment manually:
   source client/venv/bin/activate
   python task_manager_client.py
   
   # Or use the convenience script:
   ./run_client.sh
   ```

## Usage

### Interactive Mode

The client provides an interactive menu with the following options:

```
📋 MAIN MENU:
Auth:
  1. Register new user
  2. Login
  3. Logout

Tasks:
  4. Get all tasks
  5. Get task by ID
  6. Create task
  7. Update task
  8. Delete task

Users:
  9. Get all users

Monitoring:
  10. Check application health
  11. Get metrics overview

Testing:
  12. Run complete test scenario

Other:
  13. Show current user info
  0. Exit
```

### Test Scenario

Option 12 runs a comprehensive test that:

1. ✅ Checks application health
2. ✅ Registers a new test user
3. ✅ Creates sample tasks
4. ✅ Retrieves all tasks
5. ✅ Updates a task (marks as completed)
6. ✅ Gets task by ID
7. ✅ Gets all users
8. ✅ Checks application metrics
9. ✅ Cleans up test data

This is perfect for quickly verifying that all your API endpoints are working correctly!

### Authentication Flow

1. **Register a new user:**
   - Provide username, email, first name, last name, and password
   - Automatically logs you in upon successful registration

2. **Login with existing user:**
   - Provide username and password
   - JWT token is stored for subsequent API calls

3. **All GraphQL operations require authentication:**
   - Tasks and user operations are protected
   - The client automatically includes the JWT token in requests

### Default Test Users

Your application creates these default users (from DataLoader configuration):

- **Username**: `admin`, **Password**: `admin123` (ADMIN role)
- **Username**: `user`, **Password**: `user123` (USER role)

You can login with these credentials to test different permission levels.

## API Endpoints Tested

### REST Authentication Endpoints

- `POST /auth/register` - Register new user
- `POST /auth/login` - Login user

### GraphQL Endpoints

#### Queries
- `tasks` - Get all tasks
- `task(id)` - Get task by ID
- `users` - Get all users

#### Mutations
- `createTask(input)` - Create new task
- `updateTask(id, input)` - Update existing task
- `deleteTask(id)` - Delete task

### Actuator Endpoints

- `GET /actuator/health` - Application health check
- `GET /actuator/metrics` - Available metrics

## Example Usage Session

```bash
$ python task_manager_client.py

🚀 TASK MANAGER CLI CLIENT
=========================================
🔍 Checking if Task Manager server is running...
✅ Application health: UP

📋 MAIN MENU:
...

👤 Not logged in
Enter your choice (0-13): 2

Username: user
Password: user123
✅ Login successful! Welcome back user (USER)

👤 Logged in as: user (USER)
Enter your choice (0-13): 6

Task title: Review API documentation
Task description: Review and update the API documentation for completeness
Due date (YYYY-MM-DD, optional): 2024-12-15
Estimation hours (optional): 4
✅ Created task: Review API documentation (ID: 42)

👤 Logged in as: user (USER)
Enter your choice (0-13): 4

✅ Retrieved 3 tasks
  - [42] Review API documentation - TODO
  - [43] Complete project setup - IN_PROGRESS (assigned to: admin)
  - [44] Write unit tests - TODO
```

## Error Handling

The client handles various error scenarios:

- **Server not running**: Checks health before starting
- **Authentication failures**: Clear error messages for login/register issues
- **GraphQL errors**: Displays GraphQL validation and execution errors
- **Network issues**: Timeout and connection error handling
- **Invalid input**: Validates user input and provides feedback

## Features in Detail

### JWT Token Management
- Automatically stores JWT token after login/register
- Includes token in all GraphQL requests
- Token is displayed (partially) in user info

### Request Correlation
- All requests generate correlation IDs (server-side)
- Check your server logs to see the correlation tracking in action

### Metrics Integration
- Health check integration
- Metrics overview to see available Prometheus metrics
- Perfect for testing your observability implementation

### User Experience
- Color-coded output (✅ success, ❌ error)
- Clear menu structure
- Graceful error handling
- Keyboard interrupt handling (Ctrl+C)

## Troubleshooting

### Common Issues

1. **"requests library required"**
   ```bash
   pip install requests
   ```

2. **"Task Manager server not running"**
   ```bash
   # Start the server first
   mvn spring-boot:run
   ```

3. **"Authentication failed"**
   - Check username/password
   - Try with default users: `admin/admin123` or `user/user123`

4. **"GraphQL errors: Unauthorized"**
   - Make sure you're logged in (option 2)
   - Check that your user has the right permissions

### Server Logs

When running tests, check your server console for:
- Structured logging with correlation IDs
- Metrics being incremented
- Security events (login success/failure)
- Request tracing information

## Advanced Usage

### Programmatic Usage

You can also use the `TaskManagerClient` class programmatically:

```python
from task_manager_client import TaskManagerClient

client = TaskManagerClient()

# Login
client.login("user", "user123")

# Create a task
task = client.create_task(
    title="Automated task",
    description="Created via script",
    due_date="2024-12-31",
    estimation_hours=2
)

# Get all tasks
tasks = client.get_tasks()
print(f"Total tasks: {len(tasks)}")
```

This makes it easy to create automated tests or integration scripts.

## Contributing

Feel free to extend the client with additional features:
- Batch operations
- Task filtering and searching
- User management (if you add admin endpoints)
- Export/import functionality
- Configuration file support

The client is designed to be easily extensible while maintaining simplicity.
