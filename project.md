# Java GraphQL Task Manager

## 1. Tech Stack
- **Java 17+** (or latest LTS)  
- **Spring Boot** (for rapid API setup)  
- **Spring GraphQL** (GraphQL integration)  
- **SQL Database** (Docker instance)  
- **Maven** (build & dependencies)  

---

## 2. Project Structure

```
task-manager/
├── src/
│ └── main/
│ ├── java/
│ │ └── com/
│ │ └── example/
│ │ └── taskmanager/
│ │ ├── controller/ # GraphQL controllers/resolvers
│ │ ├── model/ # Task entity
│ │ ├── repository/ # JPA repository
│ │ └── service/ # Business logic
│ └── resources/
│ ├── application.properties
│ └── schema.graphqls # GraphQL schema
└── pom.xml
```