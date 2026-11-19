# services

Microservicios de negocio de `casa`. Cada carpeta es un proyecto Spring Boot independiente, con su propia base de datos PostgreSQL y su propio Maven Wrapper.

## Antes de arrancar un servicio

1. Levanta la infraestructura en DEV, en este reserva: `casa-config`, `casa-eureka` y `casa-gateway` (ver [`infra/README.md`](../infra/README.md)). Los servicios leen su configuración de `casa-config` (`http://localhost:18888`) y se registran en `casa-eureka`.
2. Docker Desktop encendido: en DEV solo se usa para PostgreSQL. La aplicación corre con Maven Wrapper en el host.

## Servicios

| Carpeta | Qué es | Desde qué sesión | Puerto DEV (app) | Base de datos DEV |
|---|---|---|---|---|
| `casa-propiedad-ms/` | Categorías y propiedades. | S1 | `8080` (2.ª instancia: `8081`) | `casa_propiedad_db` en `15432` |
| `casa-reserva-ms/` | Órdenes; llama a catálogo con Feign y Circuit Breaker. | S2 (base autónoma), S6 | `8082` (2.ª instancia: `8083`) | `casa_reserva_db` en `15434` |
| `casa-cliente-ms/` | Perfil del cliente (DNI/RUC). | S2 (trabajo autónomo) | `8084` | `casa_cliente_db` en `15433` |
| `casa-auth-ms/` | Autenticación y emisión de JWT (temporal, luego Keycloak). | S7 | `8085` | `casa_auth_db` en `15431` |
| `casa-pago-ms/` | Pagos; consume `reserva-eventos` y publica `pago-eventos`. | S8 | `8086` | `casa_pago_db` en `15435` |

La carpeta de `casa-cliente-ms` aparece cuando esa sesión la construye. Los comandos de abajo aplican a cada una en cuanto exista.

## Ejecutar en DEV

Cada comando se ejecuta parado en la carpeta del servicio. Primero la base de datos, después la aplicación.

### `casa-propiedad-ms`

```powershell
cd services/casa-propiedad-ms
docker compose -f compose-dev.yml up -d
.\mvnw.cmd spring-boot:run
```

Swagger: `http://localhost:8080/swagger-ui.html`. Health: `http://localhost:8080/actuator/health`.

Segunda instancia, en otra terminal (puerto fijo, no aleatorio):

```powershell
cd services/casa-propiedad-ms
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--server.port=8081"
```

### `casa-reserva-ms`

```powershell
cd services/casa-reserva-ms
docker compose -f compose-dev.yml up -d
.\mvnw.cmd spring-boot:run
```

Swagger: `http://localhost:8082/swagger-ui.html`. Health: `http://localhost:8082/actuator/health`. Segunda instancia con `"-Dspring-boot.run.arguments=--server.port=8083"`, igual que arriba.

### `casa-cliente-ms`

```powershell
cd services/casa-cliente-ms
docker compose -f compose-dev.yml up -d
.\mvnw.cmd spring-boot:run
```

Swagger: `http://localhost:8084/swagger-ui.html`. Health: `http://localhost:8084/actuator/health`.

### `casa-auth-ms`

```powershell
cd services/casa-auth-ms
docker compose -f compose-dev.yml up -d
.\mvnw.cmd spring-boot:run
```

Login: `POST http://localhost:8085/api/v1/auth/login`. Registro de un usuario nuevo (rol `CLIENTE`): `POST http://localhost:8085/api/v1/auth/registro`. Swagger: `http://localhost:8085/swagger-ui.html`. Claves públicas: `http://localhost:8085/.well-known/jwks.json`. Health: `http://localhost:8085/actuator/health`.

### `casa-pago-ms`

Necesita Kafka corriendo (`kafka/compose-dev.yml`, ver el README de la raíz).

```powershell
cd services/casa-pago-ms
docker compose -f compose-dev.yml up -d
.\mvnw.cmd spring-boot:run
```

Consume `reserva-eventos` y publica `pago-eventos`. No expone endpoints de negocio: su evidencia es el log, Kafka UI (`http://localhost:18085`) y la tabla `pagos`. Health: `http://localhost:8086/actuator/health`.

En macOS/Linux, el equivalente de cada comando es `./mvnw spring-boot:run`.

## Detener

```powershell
docker compose -f compose-dev.yml down
```

Ejecutado en la carpeta del servicio, apaga su PostgreSQL y conserva los datos (el volumen sigue existiendo). La aplicación se detiene con `Ctrl+C` en su terminal.

## Producción local

Cada servicio publica su propio `compose.yml` cuando esa sesión lo pide (hoy, solo `casa-propiedad-ms`). En producción local ningún microservicio expone su puerto al host: todo el tráfico entra por `casa-gateway`. Detalle en el `README.md` de cada servicio y en las guías de sesión.
