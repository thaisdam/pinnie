FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
# Make mvnw executable just in case
RUN chmod +x ./mvnw
# Download dependencies
RUN ./mvnw dependency:go-offline
COPY src ./src
# Build ignoring tests (already tested in CI)
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
