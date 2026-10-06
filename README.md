# Users Management — Spring Boot, Arquitectura Hexagonal y DDD

Aplicación de gestión de usuarios construida con **Java 17** y **Spring Boot 3.3.5**.
La **API REST** es el punto de entrada activo, desacoplada mediante **Arquitectura Hexagonal (Ports & Adapters)** y principios de **Domain-Driven Design (DDD)**.

Spring es el único *composition root*: `Main` inicia el contexto y las dependencias se resuelven mediante configuración y component scanning de Spring.

---

## 🚀 Arquitectura y Adaptadores de Base de Datos

El sistema implementa persistencia desacoplada mediante puertos de salida (`SaveUserPort`, `GetUserByIdPort`, `GetUserByEmailPort`, `GetAllUsersPort`, `UpdateUserPort`, `DeleteUserPort`) con dos adaptadores disponibles:

1. **MySQL**: Implementado en `UserRepositoryMySQL` (activado por defecto o con `db.engine=mysql`).
2. **PostgreSQL**: Implementado en `UserRepositoryPostgreSQL` (activado con `db.engine=postgresql`).

La selección de motor y parámetros de conexión se configuran mediante `application.properties` o variables de entorno:

```properties
db.engine=${DB_ENGINE:mysql}    # 'mysql' o 'postgresql'
db.host=${DB_HOST:localhost}
db.port=${DB_PORT:3306}         # 3306 para MySQL, 5432 para PostgreSQL
db.name=${DB_NAME:crud_usuarios}
db.username=${DB_USERNAME:root}
db.password=${DB_PASSWORD:}
db.ssl-mode=${DB_SSL_MODE:DISABLED} # DISABLED o REQUIRED
```

---

## 🛠️ Prerrequisitos

- **JDK 17 LTS** (Temurin 17 recomendado)
- **Apache Maven 3.9+** (o el wrapper `mvnw.cmd` / `./mvnw`)
- **Git**
- **MySQL** (ej. XAMPP o Aiven) y/o **PostgreSQL** (ej. Render PostgreSQL, Supabase o local)
- **Docker** (opcional para ejecución en contenedores)

---

## ⚙️ Inicialización de Base de Datos

### MySQL
Ejecutar el script SQL en el servidor MySQL:
```sql
-- Ubicado en: src/main/resources/schema.sql
```

### PostgreSQL
Ejecutar el script SQL en el servidor PostgreSQL (o la consola de Supabase/Render):
```sql
-- Ubicado en: src/main/resources/schema-postgresql.sql
```

Ambos scripts crean la base de datos `crud_usuarios`, la tabla `users` y el usuario inicial:
- **Email:** `admin@example.com`
- **Contraseña:** `Admin1234!`
- **Rol:** `ADMIN`
- **Estado:** `ACTIVE`

---

## 🧪 Compilación y Pruebas Unitarias

Para compilar y verificar que todas las pruebas pasen:

```bash
mvn clean test
```
En Windows:
```powershell
.\mvnw.cmd clean test
```

Para empaquetar el artefacto JAR:
```bash
mvn clean package -DskipTests
```

---

## 🏃 Ejecución Local

Para iniciar el microservicio en el puerto `8080`:

```bash
mvn spring-boot:run
```
o
```powershell
.\mvnw.cmd spring-boot:run
```

Una vez iniciado, accede a:
- **Swagger UI:** [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- **Documentación OpenAPI:** [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

---

## 🐳 Ejecución con Docker

### Con MySQL
```bash
docker compose up --build -d
```

### Con PostgreSQL
```bash
docker compose -f compose.postgres.yaml up --build -d
```

---

## ☁️ Despliegue en Render Cloud

El archivo `render.yaml` define la infraestructura automatizada (Infrastructure as Code) para Render:

1. Ingresa a tu cuenta de [Render Dashboard](https://dashboard.render.com/).
2. Haz clic en **New +** y selecciona **Blueprint**.
3. Conecta tu repositorio forkeado: `users-management-spring-boot-hexagonal-ddd`.
4. Render leerá `render.yaml` y construirá el servicio web Docker.
5. Configura las variables de entorno en el panel de Render:
   - `DB_ENGINE`: `mysql` o `postgresql`.
   - `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`.
   - `DB_SSL_MODE`: `REQUIRED`.
   - `SMTP_USERNAME`, `SMTP_PASSWORD`, `SMTP_FROM_ADDRESS` (credenciales de Gmail con contraseña de aplicación de 16 dígitos).
6. Ejecuta el script de esquema (`schema.sql` para MySQL o `schema-postgresql.sql` para Postgres) en tu base de datos remota una sola vez.
7. Tu API quedará disponible en el subdominio `https://<tu-servicio>.onrender.com` con Swagger UI en `/swagger-ui/index.html`.
