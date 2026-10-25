FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /src
COPY pom.xml .
RUN mvn -q -B dependency:go-offline || true
COPY src ./src
RUN mvn -q -B -DskipTests package && cp target/*.war /app.war

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app.war /app/app.war
ENV JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8" SPRING_PROFILES_ACTIVE=cloud PORT=8080
EXPOSE 8080
ENTRYPOINT ["sh","-c","exec java -Dfile.encoding=UTF-8 -jar /app/app.war --server.port=${PORT:-8080}"]
