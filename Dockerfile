# Use an official JDK runtime as a parent image
FROM openjdk:17-jdk-slim

# Set the working directory in the container
WORKDIR /app

# Copy the Gradle wrapper and build script
COPY gradlew .
COPY gradle /gradle

# Copy the rest of the project
COPY . .

# Make the Gradle wrapper executable
RUN chmod +x gradlew

# Build the project without running tests
RUN ./gradlew build --no-daemon -x test

# Copy the built jar file to the container
COPY build/libs/*.jar app.jar

# Expose the port the app runs on
EXPOSE 8080

# Run the jar file
ENTRYPOINT ["java","-jar","/app/app.jar"]
