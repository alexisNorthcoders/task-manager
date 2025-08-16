# Use a lightweight OpenJDK image
FROM eclipse-temurin:21-jre

# Set workdir
WORKDIR /app

# Copy built jar (replace with your actual jar name if different)
COPY target/taskmanager-0.0.1-SNAPSHOT.jar app.jar

# Expose port
EXPOSE 8080

# Run the app
ENTRYPOINT ["java", "-jar", "app.jar"]