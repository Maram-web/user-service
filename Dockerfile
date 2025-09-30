# Étape 1 : Build avec Maven
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Étape 2 : Exécution
FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

#  Plus besoin de copier manuellement les fichiers config

ENV SPRING_PROFILES_ACTIVE=k8s
ENTRYPOINT ["java", "-jar", "app.jar"]
