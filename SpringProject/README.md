# SpringProject

Spring Boot web application for registration and login workflows. Sensitive configuration values (database URL, username, password, etc.) must be supplied through environment variables before running.

## Configuration

1. Copy `src/main/resources/application-example.properties` to a private location (or `.properties` file) and fill in your actual datasource credentials.
2. Export the values before starting the application, for example:

```
set SPRING_DATASOURCE_URL=jdbc:mariadb://localhost:3306/springproject
set SPRING_DATASOURCE_USERNAME=your_user
set SPRING_DATASOURCE_PASSWORD=your_password
```

3. (Optional) To generate a bcrypt hash for a password, set `BCRYPT_SEED_PASSWORD` before launching. The application logs when a hash is generated without exposing the plain text password.

## Build

```
./mvnw clean package
```

```
./mvnw spring-boot:run
```
