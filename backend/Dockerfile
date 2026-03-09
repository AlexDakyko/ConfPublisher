# ---------- build stage: собираем приложение ----------
    FROM maven:3.9-eclipse-temurin-17 AS build
    WORKDIR /app
    
    # Сначала зависимости (кэшируется)
    COPY pom.xml .
    RUN mvn -q -DskipTests dependency:go-offline
    
    # Затем исходники и сборка
    COPY src ./src
    RUN mvn -q -DskipTests package
    
    # ---------- run stage: запускаем собранный JAR ----------
    FROM eclipse-temurin:17-jre
    WORKDIR /app
    
    # Копируем готовый JAR из предыдущего этапа
    COPY --from=build /app/target/*.jar /app/app.jar
    
    # Порт сервиса внутри контейнера
    EXPOSE 8080
    
    # Запуск
    ENTRYPOINT ["java","-jar","/app/app.jar"]