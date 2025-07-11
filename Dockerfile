FROM maven:3.9.7-eclipse-temurin-21 AS builder
WORKDIR /workspace

# Copy Maven project descriptor first and resolve dependencies
COPY MediSchool-BE/pom.xml ./pom.xml
COPY MediSchool-BE/.mvn ./.mvn
COPY MediSchool-BE/mvnw ./mvnw
RUN chmod +x mvnw && ./mvnw -B dependency:go-offline

# Copy the source code
COPY MediSchool-BE/src ./src

# Build the application (skip tests for faster CI/CD)
RUN ./mvnw -B package -DskipTests

# ------------------------
# Runtime image
# ------------------------
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy the fat/boot jar from the builder stage
COPY --from=builder /workspace/target/*SNAPSHOT.jar application.jar

# Expose default Spring Boot port
EXPOSE 8080

# Improve JVM ergonomics in containers
ENV JAVA_TOOL_OPTIONS "-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

ENTRYPOINT ["java","-jar","/app/application.jar"]