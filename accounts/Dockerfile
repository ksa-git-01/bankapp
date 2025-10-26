FROM maven:3.9.9-amazoncorretto-21 AS build

WORKDIR /app

# Копируем все pom
COPY pom.xml .
COPY accounts/pom.xml ./accounts/
COPY gateway/pom.xml ./gateway/
COPY frontui/pom.xml ./frontui/
COPY cash/pom.xml ./cash/
COPY blocker/pom.xml ./blocker/
COPY transfer/pom.xml ./transfer/
COPY notifications/pom.xml ./notifications/
COPY exchange/pom.xml ./exchange/
COPY generator/pom.xml ./generator/

# Скачиваем зависимости
RUN mvn dependency:go-offline -B

# Копируем исходники
COPY accounts/src ./accounts/src

# Собираем только accounts модуль
WORKDIR /app/accounts
RUN mvn clean package -DskipTests

FROM amazoncorretto:21
WORKDIR /app

COPY --from=build /app/accounts/target/*.jar app.jar

EXPOSE 8080
CMD ["java", "-jar", "app.jar"]