FROM eclipse-temurin:21-jdk
WORKDIR /app

# Copy wrapper scripts and Gradle directory
COPY gradlew gradlew.bat ./
COPY gradle/ gradle/
RUN chmod +x gradlew

# Diagnostic: list the contents of the gradle directory inside the container
RUN ls -R gradle || true

# Copy Gradle config files
COPY settings.gradle gradle.properties ./
COPY app/build.gradle app/build.gradle
# Attempt dependency resolution
RUN ./gradlew dependencies --no-daemon

# Copy rest of the source
COPY . .

# Build the project
RUN ./gradlew build --no-daemon -x test

EXPOSE 8080
CMD ["./gradlew", "run", "--no-daemon"]
