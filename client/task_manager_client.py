#!/usr/bin/env python3
"""
Task Manager CLI Client
A simple command-line client to test the Task Manager GraphQL API with JWT authentication.
"""

import requests
import json
import sys
import os
from datetime import datetime
from typing import Optional, Dict, Any

class TaskManagerClient:
    def __init__(self, base_url: str = "http://localhost:8080"):
        self.base_url = base_url
        self.auth_url = f"{base_url}/auth"
        self.graphql_url = f"{base_url}/graphql"
        self.token: Optional[str] = None
        self.user_info: Optional[Dict[str, Any]] = None
        
    def _make_request(self, method: str, url: str, data: Optional[Dict] = None, headers: Optional[Dict] = None) -> Dict:
        """Make HTTP request with error handling"""
        try:
            if headers is None:
                headers = {"Content-Type": "application/json"}
            
            response = requests.request(method, url, json=data, headers=headers, timeout=10)
            
            if response.status_code == 200:
                return {"success": True, "data": response.json()}
            else:
                return {"success": False, "error": f"HTTP {response.status_code}: {response.text}"}
                
        except requests.exceptions.RequestException as e:
            return {"success": False, "error": f"Request failed: {str(e)}"}
    
    def _graphql_request(self, query: str, variables: Optional[Dict] = None) -> Dict:
        """Make GraphQL request with authentication"""
        headers = {"Content-Type": "application/json"}
        if self.token:
            headers["Authorization"] = f"Bearer {self.token}"
        
        payload = {"query": query}
        if variables:
            payload["variables"] = variables
            
        return self._make_request("POST", self.graphql_url, payload, headers)
    
    def register(self, username: str, email: str, first_name: str, last_name: str, password: str) -> bool:
        """Register a new user"""
        data = {
            "username": username,
            "email": email,
            "firstName": first_name,
            "lastName": last_name,
            "password": password
        }
        
        result = self._make_request("POST", f"{self.auth_url}/register", data)
        
        if result["success"]:
            auth_data = result["data"]
            self.token = auth_data["token"]
            self.user_info = {
                "username": auth_data["username"],
                "email": auth_data["email"],
                "role": auth_data["role"]
            }
            print(f"✅ Registration successful! Welcome {auth_data['username']} ({auth_data['role']})")
            return True
        else:
            print(f"❌ Registration failed: {result['error']}")
            return False
    
    def login(self, username: str, password: str) -> bool:
        """Login user"""
        data = {"username": username, "password": password}
        
        result = self._make_request("POST", f"{self.auth_url}/login", data)
        
        if result["success"]:
            auth_data = result["data"]
            self.token = auth_data["token"]
            self.user_info = {
                "username": auth_data["username"],
                "email": auth_data["email"],
                "role": auth_data["role"]
            }
            print(f"✅ Login successful! Welcome back {auth_data['username']} ({auth_data['role']})")
            return True
        else:
            print(f"❌ Login failed: {result['error']}")
            return False
    
    def logout(self):
        """Logout user"""
        self.token = None
        self.user_info = None
        print("✅ Logged out successfully")
    
    def get_tasks(self) -> Optional[list]:
        """Get all tasks"""
        query = """
        query {
            tasks {
                id
                title
                description
                completed
                status
                dueDate
                estimationHours
                createdAt
                updatedAt
            }
        }
        """
        
        result = self._graphql_request(query)
        
        if result["success"]:
            if "errors" in result["data"]:
                print(f"❌ GraphQL errors: {result['data']['errors']}")
                return None
            
            tasks = result["data"]["data"]["tasks"]
            print(f"✅ Retrieved {len(tasks)} tasks")
            return tasks
        else:
            print(f"❌ Failed to get tasks: {result['error']}")
            return None
    
    def get_task_by_id(self, task_id: int) -> Optional[Dict]:
        """Get task by ID"""
        query = """
        query($id: ID!) {
            task(id: $id) {
                id
                title
                description
                completed
                status
                dueDate
                estimationHours
                createdAt
                updatedAt
            }
        }
        """
        
        result = self._graphql_request(query, {"id": str(task_id)})
        
        if result["success"] and "errors" not in result["data"]:
            task = result["data"]["data"]["task"]
            if task:
                print(f"✅ Retrieved task: {task['title']}")
                return task
            else:
                print(f"❌ Task with ID {task_id} not found")
                return None
        else:
            print(f"❌ Failed to get task: {result.get('error', result['data'].get('errors', 'Unknown error'))}")
            return None
    
    def create_task(self, title: str, description: str, due_date: Optional[str] = None, 
                   estimation_hours: Optional[int] = None, assigned_user_ids: Optional[list] = None) -> Optional[Dict]:
        """Create a new task"""
        mutation = """
        mutation($input: CreateTaskInput!) {
            createTask(input: $input) {
                id
                title
                description
                completed
                status
                dueDate
                estimationHours
                createdAt
            }
        }
        """
        
        input_data = {
            "title": title,
            "description": description,
            "completed": False
        }
        
        if due_date:
            input_data["dueDate"] = due_date
        if estimation_hours:
            input_data["estimationHours"] = estimation_hours
        if assigned_user_ids:
            input_data["assignedUserIds"] = assigned_user_ids
        
        result = self._graphql_request(mutation, {"input": input_data})
        
        if result["success"] and "errors" not in result["data"]:
            task = result["data"]["data"]["createTask"]
            print(f"✅ Created task: {task['title']} (ID: {task['id']})")
            return task
        else:
            print(f"❌ Failed to create task: {result.get('error', result['data'].get('errors', 'Unknown error'))}")
            return None
    
    def update_task(self, task_id: int, title: Optional[str] = None, description: Optional[str] = None,
                   completed: Optional[bool] = None, due_date: Optional[str] = None,
                   estimation_hours: Optional[int] = None) -> Optional[Dict]:
        """Update an existing task"""
        mutation = """
        mutation($id: ID!, $input: UpdateTaskInput!) {
            updateTask(id: $id, input: $input) {
                id
                title
                description
                completed
                status
                dueDate
                estimationHours
                updatedAt
            }
        }
        """
        
        input_data = {}
        if title is not None:
            input_data["title"] = title
        if description is not None:
            input_data["description"] = description
        if completed is not None:
            input_data["completed"] = completed
        if due_date is not None:
            input_data["dueDate"] = due_date
        if estimation_hours is not None:
            input_data["estimationHours"] = estimation_hours
        
        result = self._graphql_request(mutation, {"id": str(task_id), "input": input_data})
        
        if result["success"] and "errors" not in result["data"]:
            task = result["data"]["data"]["updateTask"]
            print(f"✅ Updated task: {task['title']} (ID: {task['id']})")
            return task
        else:
            print(f"❌ Failed to update task: {result.get('error', result['data'].get('errors', 'Unknown error'))}")
            return None
    
    def delete_task(self, task_id: int) -> bool:
        """Delete a task"""
        mutation = """
        mutation($id: ID!) {
            deleteTask(id: $id)
        }
        """
        
        result = self._graphql_request(mutation, {"id": str(task_id)})
        
        if result["success"] and "errors" not in result["data"]:
            success = result["data"]["data"]["deleteTask"]
            if success:
                print(f"✅ Deleted task with ID: {task_id}")
                return True
            else:
                print(f"❌ Failed to delete task with ID: {task_id}")
                return False
        else:
            print(f"❌ Failed to delete task: {result.get('error', result['data'].get('errors', 'Unknown error'))}")
            return False
    
    def get_users(self) -> Optional[list]:
        """Get all users"""
        query = """
        query {
            users {
                id
                username
                email
                firstName
                lastName
                createdAt
                assignedTasks {
                    id
                    title
                }
            }
        }
        """
        
        result = self._graphql_request(query)
        
        if result["success"]:
            if "errors" in result["data"]:
                print(f"❌ GraphQL errors: {result['data']['errors']}")
                return None
            
            users = result["data"]["data"]["users"]
            print(f"✅ Retrieved {len(users)} users")
            return users
        else:
            print(f"❌ Failed to get users: {result['error']}")
            return None
    
    def check_health(self) -> bool:
        """Check application health"""
        try:
            response = requests.get(f"{self.base_url}/actuator/health", timeout=5)
            if response.status_code == 200:
                health = response.json()
                status = health.get("status", "UNKNOWN")
                print(f"✅ Application health: {status}")
                return status == "UP"
            else:
                print(f"❌ Health check failed: HTTP {response.status_code}")
                return False
        except Exception as e:
            print(f"❌ Health check failed: {str(e)}")
            return False
    
    def get_metrics(self) -> Optional[Dict]:
        """Get application metrics"""
        try:
            response = requests.get(f"{self.base_url}/actuator/metrics", timeout=5)
            if response.status_code == 200:
                metrics = response.json()
                print(f"✅ Available metrics: {len(metrics.get('names', []))}")
                return metrics
            else:
                print(f"❌ Metrics request failed: HTTP {response.status_code}")
                return None
        except Exception as e:
            print(f"❌ Metrics request failed: {str(e)}")
            return None


def print_header():
    """Print application header"""
    print("=" * 60)
    print("🚀 TASK MANAGER CLI CLIENT")
    print("=" * 60)
    print("A simple client to test your Task Manager GraphQL API")
    print()


def print_menu():
    """Print main menu"""
    print("\n📋 MAIN MENU:")
    print("Auth:")
    print("  1. Register new user")
    print("  2. Login")
    print("  3. Logout")
    print("\nTasks:")
    print("  4. Get all tasks")
    print("  5. Get task by ID")
    print("  6. Create task")
    print("  7. Update task")
    print("  8. Delete task")
    print("\nUsers:")
    print("  9. Get all users")
    print("\nMonitoring:")
    print("  10. Check application health")
    print("  11. Get metrics overview")
    print("\nTesting:")
    print("  12. Run complete test scenario")
    print("\nOther:")
    print("  13. Show current user info")
    print("  0. Exit")
    print()


def run_test_scenario(client: TaskManagerClient):
    """Run a complete test scenario"""
    print("\n🧪 RUNNING COMPLETE TEST SCENARIO")
    print("=" * 50)
    
    # Check health first
    print("\n1. Checking application health...")
    if not client.check_health():
        print("❌ Application is not healthy. Stopping test.")
        return
    
    # Register a test user
    print("\n2. Registering test user...")
    test_user = f"testuser_{datetime.now().strftime('%H%M%S')}"
    if not client.register(test_user, f"{test_user}@test.com", "Test", "User", "testpass123"):
        print("❌ Registration failed. Stopping test.")
        return
    
    # Create some tasks
    print("\n3. Creating test tasks...")
    task1 = client.create_task(
        title="Complete project documentation",
        description="Write comprehensive documentation for the project",
        due_date="2024-12-31",
        estimation_hours=8
    )
    
    task2 = client.create_task(
        title="Review code changes",
        description="Review and approve pending code changes",
        estimation_hours=4
    )
    
    if not task1 or not task2:
        print("❌ Task creation failed. Continuing with other tests...")
    
    # Get all tasks
    print("\n4. Retrieving all tasks...")
    tasks = client.get_tasks()
    
    # Update a task
    if task1:
        print(f"\n5. Updating task {task1['id']}...")
        client.update_task(task1["id"], completed=True)
    
    # Get task by ID
    if task1:
        print(f"\n6. Getting task {task1['id']} by ID...")
        client.get_task_by_id(task1["id"])
    
    # Get all users
    print("\n7. Retrieving all users...")
    client.get_users()
    
    # Check metrics
    print("\n8. Checking application metrics...")
    client.get_metrics()
    
    # Clean up - delete created tasks
    if task2:
        print(f"\n9. Cleaning up - deleting task {task2['id']}...")
        client.delete_task(task2["id"])
    
    print("\n✅ Test scenario completed successfully!")
    print("Check your application logs for correlation IDs and metrics.")


def main():
    """Main CLI loop"""
    print_header()
    
    # Check if requests is available
    try:
        import requests
    except ImportError:
        print("❌ Error: 'requests' library is required.")
        print("Install it with: pip install requests")
        sys.exit(1)
    
    client = TaskManagerClient()
    
    # Check if server is running
    print("🔍 Checking if Task Manager server is running...")
    if not client.check_health():
        print("❌ Task Manager server is not running or not accessible at http://localhost:8080")
        print("Please start the server with: mvn spring-boot:run")
        sys.exit(1)
    
    while True:
        print_menu()
        
        if client.user_info:
            print(f"👤 Logged in as: {client.user_info['username']} ({client.user_info['role']})")
        else:
            print("👤 Not logged in")
        
        try:
            choice = input("\nEnter your choice (0-13): ").strip()
            
            if choice == "0":
                print("👋 Goodbye!")
                break
            
            elif choice == "1":
                username = input("Username: ")
                email = input("Email: ")
                first_name = input("First Name: ")
                last_name = input("Last Name: ")
                password = input("Password: ")
                client.register(username, email, first_name, last_name, password)
            
            elif choice == "2":
                username = input("Username: ")
                password = input("Password: ")
                client.login(username, password)
            
            elif choice == "3":
                client.logout()
            
            elif choice == "4":
                tasks = client.get_tasks()
                if tasks:
                    for task in tasks:
                        assigned_users = [u['username'] for u in task.get('assignedUsers', [])]
                        assigned_str = f" (assigned to: {', '.join(assigned_users)})" if assigned_users else ""
                        print(f"  - [{task['id']}] {task['title']} - {task['status']}{assigned_str}")
            
            elif choice == "5":
                task_id = input("Task ID: ")
                try:
                    task = client.get_task_by_id(int(task_id))
                    if task:
                        print(f"Title: {task['title']}")
                        print(f"Description: {task['description']}")
                        print(f"Status: {task['status']}")
                        print(f"Due Date: {task.get('dueDate', 'Not set')}")
                        print(f"Estimation: {task.get('estimationHours', 'Not set')} hours")
                except ValueError:
                    print("❌ Invalid task ID")
            
            elif choice == "6":
                title = input("Task title: ")
                description = input("Task description: ")
                due_date = input("Due date (YYYY-MM-DD, optional): ") or None
                estimation = input("Estimation hours (optional): ")
                estimation_hours = int(estimation) if estimation else None
                client.create_task(title, description, due_date, estimation_hours)
            
            elif choice == "7":
                task_id = input("Task ID to update: ")
                try:
                    print("Leave fields empty to keep current values:")
                    title = input("New title: ") or None
                    description = input("New description: ") or None
                    completed_input = input("Completed (true/false): ")
                    completed = None
                    if completed_input.lower() in ['true', 't', 'yes', 'y']:
                        completed = True
                    elif completed_input.lower() in ['false', 'f', 'no', 'n']:
                        completed = False
                    
                    due_date = input("New due date (YYYY-MM-DD): ") or None
                    estimation = input("New estimation hours: ")
                    estimation_hours = int(estimation) if estimation else None
                    
                    client.update_task(int(task_id), title, description, completed, due_date, estimation_hours)
                except ValueError:
                    print("❌ Invalid task ID or estimation hours")
            
            elif choice == "8":
                task_id = input("Task ID to delete: ")
                try:
                    confirm = input(f"Are you sure you want to delete task {task_id}? (y/N): ")
                    if confirm.lower() in ['y', 'yes']:
                        client.delete_task(int(task_id))
                    else:
                        print("Deletion cancelled")
                except ValueError:
                    print("❌ Invalid task ID")
            
            elif choice == "9":
                users = client.get_users()
                if users:
                    for user in users:
                        task_count = len(user.get('assignedTasks', []))
                        print(f"  - [{user['id']}] {user['username']} ({user['email']}) - {task_count} tasks")
            
            elif choice == "10":
                client.check_health()
            
            elif choice == "11":
                metrics = client.get_metrics()
                if metrics:
                    print("Sample metrics:")
                    for metric in metrics.get('names', [])[:10]:
                        print(f"  - {metric}")
                    if len(metrics.get('names', [])) > 10:
                        print(f"  ... and {len(metrics['names']) - 10} more")
            
            elif choice == "12":
                run_test_scenario(client)
            
            elif choice == "13":
                if client.user_info:
                    print(f"Username: {client.user_info['username']}")
                    print(f"Email: {client.user_info['email']}")
                    print(f"Role: {client.user_info['role']}")
                    print(f"Token: {client.token[:20]}...{client.token[-10:] if client.token else 'None'}")
                else:
                    print("Not logged in")
            
            else:
                print("❌ Invalid choice. Please try again.")
        
        except KeyboardInterrupt:
            print("\n\n👋 Goodbye!")
            break
        except Exception as e:
            print(f"❌ Error: {str(e)}")


if __name__ == "__main__":
    main()
