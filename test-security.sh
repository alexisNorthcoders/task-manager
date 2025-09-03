#!/bin/bash

# Test script for JWT authentication
BASE_URL="http://localhost:8080"

echo "=== JWT Authentication Test ==="
echo

# 1. Test registration
echo "1. Testing user registration..."
REGISTER_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "firstName": "Test",
    "lastName": "User",
    "password": "password123"
  }')

echo "Registration response: $REGISTER_RESPONSE"
echo

# 2. Test login
echo "2. Testing user login..."
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "user",
    "password": "user123"
  }')

echo "Login response: $LOGIN_RESPONSE"

# Extract token from response
TOKEN=$(echo $LOGIN_RESPONSE | grep -o '"token":"[^"]*' | cut -d'"' -f4)
echo "Extracted token: ${TOKEN:0:50}..."
echo

# 3. Test protected GraphQL endpoint without token
echo "3. Testing GraphQL without authentication (should fail)..."
UNAUTH_RESPONSE=$(curl -s -X POST "$BASE_URL/graphql" \
  -H "Content-Type: application/json" \
  -d '{
    "query": "mutation { createTask(input: { title: \"Test Task\", description: \"Test\" }) { id title } }"
  }')

echo "Unauthorized response: $UNAUTH_RESPONSE"
echo

# 4. Test protected GraphQL endpoint with token
echo "4. Testing GraphQL with authentication (should work)..."
if [ ! -z "$TOKEN" ]; then
  AUTH_RESPONSE=$(curl -s -X POST "$BASE_URL/graphql" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TOKEN" \
    -d '{
      "query": "mutation { createTask(input: { title: \"Authenticated Task\", description: \"Created with JWT\" }) { id title } }"
    }')
  
  echo "Authenticated response: $AUTH_RESPONSE"
else
  echo "No token available - skipping authenticated test"
fi
echo

# 5. Test queries (should work with authentication)
echo "5. Testing query with authentication..."
if [ ! -z "$TOKEN" ]; then
  QUERY_RESPONSE=$(curl -s -X POST "$BASE_URL/graphql" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TOKEN" \
    -d '{
      "query": "query { tasks { id title description completed } }"
    }')
  
  echo "Query response: $QUERY_RESPONSE"
else
  echo "No token available - skipping query test"
fi

echo
echo "=== Test completed ==="
