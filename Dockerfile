# Étape 1 : Build avec Maven
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Étape 2 : Création de l'image d'exécution
FROM eclipse-temurin:17-jdk
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

# ✅ Correction ici : copie directe dans le JAR si tu utilises classpath
# ATTENTION : le chemin doit correspondre au chemin réel dans le JAR !
# Le bon chemin est : /app/config/application-k8s.properties si tu l'utilises ainsi :
# --spring.config.location=classpath:/config/application-k8s.properties
RUN mkdir -p /app/config
COPY --from=build /app/src/main/resources/application.properties /app/config/
COPY --from=build /app/src/main/resources/application-k8s.properties /app/config/

# Profil par défaut
ENV SPRING_PROFILES_ACTIVE=k8s

ENTRYPOINT ["java", "-jar", "app.jar", "--spring.config.location=classpath:/config/application.properties,classpath:/config/application-k8s.properties"]
