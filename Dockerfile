# --- Stage 1: Build-Umgebung ---
FROM maven:3.9.6-eclipse-temurin-21 AS builder

    WORKDIR /app
    
    # Kopiere die pom.xml und baue die Abhängigkeiten, um den Layer-Cache zu nutzen
    COPY pom.xml .
    RUN mvn dependency:go-offline -B
    
    # Kopiere den gesamten Quellcode
    COPY src ./src
    
    # Baue die Anwendung
    RUN mvn clean install -DskipTests
    
    # --- Stage 2: Runtime-Umgebung ---
    FROM eclipse-temurin:21-jre-alpine
    
    WORKDIR /app
    
    # Kopiere das JAR-File aus der Build-Stage
    COPY --from=builder /app/target/*.jar app.jar
    
    # Exponiere den Port, auf dem die Spring Boot Anwendung standardmäßig läuft (8080)
    EXPOSE 8080
    
    # Starte die Anwendung
    ENTRYPOINT ["java", "-jar", "app.jar"]