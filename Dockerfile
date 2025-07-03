# Étape 1 : Build avec Maven
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copie des fichiers sources
COPY . .

# Compilation sans les tests
RUN mvn clean package -DskipTests

# Étape 2 : Création de l'image d'exécution
FROM eclipse-temurin:17-jdk
WORKDIR /app

# Copie du jar compilé depuis l'étape build
COPY --from=build /app/target/*.jar app.jar

# Copie des fichiers de configuration Spring
COPY --from=build /app/src/main/resources/application.properties ./config/
COPY --from=build /app/src/main/resources/application-k8s.properties ./config/application-k8s.properties

# Activation du profil k8s par défaut
ENV SPRING_PROFILES_ACTIVE=k8s

# Commande de démarrage
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.config.location=classpath:/config/application.properties,classpath:/config/application-k8s.properties"]
