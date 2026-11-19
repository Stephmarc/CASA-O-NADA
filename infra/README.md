# infra

Componentes de infraestructura compartida de `casa`: el servidor de configuración centralizada (`casa-config`, S2), el registro y descubrimiento de servicios (`casa-eureka`, S3) y el punto único de acceso (`casa-gateway`, S4).

## Responsabilidad

A diferencia de `services/`, ningún proyecto de `infra/` implementa lógica de negocio — son piezas que los microservicios de negocio (`casa-propiedad-ms`, `casa-reserva-ms`, `casa-cliente-ms`) consultan para funcionar como sistema distribuido: configuración externa, registro/descubrimiento de servicios, punto único de entrada.

## Componentes

| Carpeta | Qué es | Desde qué sesión |
|---|---|---|
| `casa-config/` | Config Server (Spring Cloud Config) — entrega la configuración por ambiente (`dev`/`prod`) de cada componente por HTTP, leyendo `config-repo/`. | S2 |
| `casa-eureka/` | Eureka Server — registro y descubrimiento de servicios. | S3 |
| `casa-gateway/` | Spring Cloud Gateway (WebMVC) — punto único de acceso y balanceo de carga (`lb://`). Desde S7 valida el JWT de `casa-auth-ms`. | S4 |

## Tecnologías

Java 21 · Spring Boot 4.0.x · Spring Cloud 2025.1.x (*Oakwood*) · Spring Boot Actuator

## Requisitos

- JDK 21
- Docker Desktop (solo para producción local — en DEV, todo corre con Maven Wrapper en el host, sin Docker)
- No hace falta instalar Maven: cada proyecto trae Maven Wrapper (`mvnw`/`mvnw.cmd`)

## Ejecutar en DEV

El **reserva importa**: `casa-config` primero (los otros dos leen su configuración de ahí), después `casa-eureka`, y por último `casa-gateway` (necesita Eureka para resolver las rutas `lb://`). Cada comando se ejecuta en su propia terminal, parado exactamente en la carpeta indicada.

**1. `casa-config`** — `http://localhost:18888`

```powershell
cd infra/casa-config
.\mvnw.cmd spring-boot:run
```

Lee `config-repo/` (carpeta local dentro del propio proyecto — no un repositorio Git remoto). Debe ejecutarse parado exactamente en `infra/casa-config`; el path de `config-repo` es relativo (`file:./config-repo`).

**2. `casa-eureka`** — `http://localhost:18761`

```powershell
cd infra/casa-eureka
.\mvnw.cmd spring-boot:run
```

El *dashboard* de Eureka queda en esa misma dirección y lista las instancias registradas.

**3. `casa-gateway`** — `http://localhost:18080`

```powershell
cd infra/casa-gateway
.\mvnw.cmd spring-boot:run
```

En macOS/Linux, el equivalente de cada comando es `./mvnw spring-boot:run`.

Verifica que los tres están activos:

```powershell
Invoke-RestMethod -Method Get -Uri "http://localhost:18888/actuator/health"
Invoke-RestMethod -Method Get -Uri "http://localhost:18761/actuator/health"
Invoke-RestMethod -Method Get -Uri "http://localhost:18080/actuator/health"
```

## Consultar la configuración de un componente

Convención de nombres en `config-repo/`: `{spring.application.name}-{perfil}.yml`.

```powershell
Invoke-RestMethod -Method Get -Uri "http://localhost:18888/casa-propiedad-ms/dev"
Invoke-RestMethod -Method Get -Uri "http://localhost:18888/casa-gateway/dev"
```

Respuesta `200 OK` con `propertySources` vacío significa que el archivo `{app}-{perfil}.yml` no existe todavía en `config-repo/`, o que el nombre no coincide letra por letra con `spring.application.name` del componente.

## Archivos de configuración (`config-repo/`)

- `casa-propiedad-ms-dev.yml` / `casa-propiedad-ms-prod.yml`
- `casa-reserva-ms-dev.yml` / `casa-reserva-ms-prod.yml`
- `casa-eureka-dev.yml` / `casa-eureka-prod.yml`
- `casa-gateway-dev.yml` / `casa-gateway-prod.yml`

`config-repo` es una carpeta local versionada en este mismo repositorio (no un Git remoto separado, a diferencia del backend `git:` nativo de Spring Cloud Config) — como `casa` es público en GitHub, **no se comitean credenciales reales aquí**: los valores actuales (`casa`/`casa`) son de prueba, para DEV/PROD local únicamente.

## Producción local con Docker

```powershell
cd infra
docker compose up -d --build
```

`infra/compose.yml` levanta hoy `casa-config` y `casa-eureka`, en la red `casa-prod-net` (que este `compose.yml` crea):

- `casa-config`: `http://localhost:28888` (host) / `http://casa-config:8888` (desde otros contenedores).
- `casa-eureka`: `http://localhost:28761` (host) / `http://casa-eureka:8761` (desde otros contenedores). Arranca solo cuando `casa-config` está saludable.

Cualquier microservicio en `services/` que necesite resolverlos por nombre debe unirse a esa misma red como `external: true` — el reserva de arranque importa: primero `infra`, después `services/*`. El servicio Docker de `casa-gateway` se agrega a este `compose.yml` en S4 (ver la guía de sesión).

Detalle completo en las guías de sesión: [S2 — configuración centralizada](../docs/sesiones/S02_Configuracion_Centralizada_Ambientes.md), [S3 — registro y descubrimiento](../docs/sesiones/S03_Registro_Descubrimiento_Ejecucion_Concurrente.md) y [S4 — punto único de acceso](../docs/sesiones/S04_Punto_Unico_Acceso_Distribucion_Trafico.md).

## Puertos

| Componente | Puerto DEV (host) | Puerto PROD local (host) |
|---|---|---|
| `casa-config` | `18888` | `28888` |
| `casa-eureka` | `18761` | `28761` |
| `casa-gateway` | `18080` | `28080` (S4) |
