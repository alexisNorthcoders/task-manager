#!/bin/bash

# Task Manager Client Runner
# Convenience script to set up and run the client

echo "🚀 Task Manager Client Setup"
echo "============================"

# Check if Python is available
if ! command -v python3 &> /dev/null; then
    echo "❌ Python 3 is required but not installed."
    echo "Please install Python 3 and try again."
    exit 1
fi

# Check if we're in the right directory
if [ ! -f "client/task_manager_client.py" ]; then
    echo "❌ Client not found. Make sure you're in the task-manager directory."
    exit 1
fi

# Set up virtual environment and dependencies
echo "📦 Setting up Python environment..."

# Create virtual environment if it doesn't exist
if [ ! -d "client/venv" ]; then
    echo "Creating virtual environment..."
    python3 -m venv client/venv
fi

# Activate virtual environment and install dependencies
echo "Installing dependencies in virtual environment..."
source client/venv/bin/activate && pip install -r client/requirements.txt > /dev/null 2>&1

if [ $? -eq 0 ]; then
    echo "✅ Dependencies installed successfully"
else
    echo "⚠️  Warning: Could not install dependencies automatically"
    echo "Try running: source client/venv/bin/activate && pip install requests"
    echo
fi

# Use virtual environment Python
PYTHON_CMD="client/venv/bin/python"

# Check if server is running
echo "🔍 Checking if Task Manager server is running..."
if curl -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
    echo "✅ Task Manager server is running"
    echo
    
    # Ask user what they want to do
    echo "What would you like to do?"
    echo "1. Run quick test (automated)"
    echo "2. Run interactive client"
    echo "3. Both (quick test first, then interactive)"
    echo
    read -p "Enter your choice (1-3): " choice
    
    case $choice in
        1)
            echo
            echo "🧪 Running quick test..."
            $PYTHON_CMD client/quick_test.py
            ;;
        2)
            echo
            echo "🖥️  Starting interactive client..."
            $PYTHON_CMD client/task_manager_client.py
            ;;
        3)
            echo
            echo "🧪 Running quick test first..."
            $PYTHON_CMD client/quick_test.py
            
            if [ $? -eq 0 ]; then
                echo
                echo "🖥️  Quick test passed! Starting interactive client..."
                echo "Press Enter to continue..."
                read
                $PYTHON_CMD client/task_manager_client.py
            else
                echo "❌ Quick test failed. Please check your server."
            fi
            ;;
        *)
            echo "Invalid choice. Starting interactive client..."
            $PYTHON_CMD client/task_manager_client.py
            ;;
    esac
else
    echo "❌ Task Manager server is not running on http://localhost:8080"
    echo
    echo "Please start the server first:"
    echo "  mvn spring-boot:run"
    echo
    echo "Then run this script again."
    exit 1
fi
