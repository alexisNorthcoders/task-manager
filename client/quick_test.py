#!/usr/bin/env python3
"""
Quick Test Script for Task Manager API
Tests basic functionality programmatically.
"""

from task_manager_client import TaskManagerClient
import sys

def main():
    print("🧪 QUICK TEST - Task Manager API")
    print("=" * 40)
    
    client = TaskManagerClient()
    
    # Test 1: Health Check
    print("1. Testing health check...")
    if not client.check_health():
        print("❌ Server is not running. Start with: mvn spring-boot:run")
        return False
    
    # Test 2: Login with default user
    print("2. Testing login with default user...")
    if not client.login("user", "user123"):
        print("❌ Login failed. Check if default users are created.")
        return False
    
    # Test 3: Create a test task
    print("3. Testing task creation...")
    task = client.create_task(
        title="Quick Test Task",
        description="This is a test task created by the quick test script",
        estimation_hours=2
    )
    
    if not task:
        print("❌ Task creation failed.")
        return False
    
    task_id = task["id"]
    
    # Test 4: Get all tasks
    print("4. Testing get all tasks...")
    tasks = client.get_tasks()
    if tasks is None:
        print("❌ Get tasks failed.")
        return False
    
    # Test 5: Update the task
    print("5. Testing task update...")
    updated_task = client.update_task(task_id, completed=True)
    if not updated_task:
        print("❌ Task update failed.")
        return False
    
    # Test 6: Get task by ID
    print("6. Testing get task by ID...")
    retrieved_task = client.get_task_by_id(task_id)
    if not retrieved_task:
        print("❌ Get task by ID failed.")
        return False
    
    # Test 7: Delete the task (cleanup)
    print("7. Testing task deletion...")
    if not client.delete_task(task_id):
        print("❌ Task deletion failed.")
        return False
    
    print("\n✅ ALL TESTS PASSED!")
    print("Your Task Manager API is working correctly.")
    print("\nTo run the interactive client: python task_manager_client.py")
    return True

if __name__ == "__main__":
    success = main()
    sys.exit(0 if success else 1)
