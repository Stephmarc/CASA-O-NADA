# Kafka

Broker de mensajes de `casa` (S8) — el primer componente que hay que
levantar antes de tocar `services/casa-reserva-ms`, `services/casa-pago-ms`
o `uso-rapido/casa-eventos-py`, porque los tres dependen de la red Docker
que este `compose-dev.yml` crea.

## Requisitos

- Docker Desktop corriendo.

## Servicios y versiones

| Servicio | Imagen | URL/Puerto (DEV) | URL/Puerto (PROD local) |
|---|---|---|---|
| Kafka broker | `apache/kafka:4.3.1` | interno `kafka:9092`, desde el host `localhost:19092` | interno `casa-kafka:9092`, desde el host `localhost:29092` |
| Kafka UI | `ghcr.io/kafbat/kafka-ui:v1.5.0` | `http://localhost:18085` | `http://localhost:28085` |

Las imágenes van con versión fija, no `latest` — quien levante este
`compose-dev.yml` meses después obtiene exactamente lo mismo que se probó al
escribir la guía de S8. `kafka-ui` usa `ghcr.io/kafbat/kafka-ui`, no
`provectuslabs/kafka-ui`: el proyecto original (Provectus) no saca una
versión real hace más de dos años — el mismo equipo continúa el desarrollo
activo bajo Kafbat.

## Uso (DEV)

Desde esta carpeta:

```powershell
docker compose -f compose-dev.yml up -d
```

Desde la raíz del repositorio:

```powershell
docker compose -f kafka/compose-dev.yml up -d
```

Contenedores esperados:

```powershell
docker compose -f compose-dev.yml ps
```

```text
casa-kafka-dev
casa-kafka-ui-dev
```

## Verificar que está arriba

Abre Kafka UI y confirma que el clúster `casa-dev` aparece **conectado**
(punto verde), sin ningún topic todavía — es normal, nadie ha publicado nada
todavía:

```text
http://localhost:18085
```

## Dos direcciones de escucha

El broker anuncia dos direcciones (`KAFKA_ADVERTISED_LISTENERS`): `INTERNAL`
(`kafka:9092`), la que usan otros contenedores de la misma red Docker (Kafka
UI, `uso-rapido/casa-eventos-py`); y `EXTERNAL` (`localhost:19092`), la que
usan los servicios Spring Boot que corren en el host con `mvnw`
(`casa-reserva-ms`, `casa-pago-ms`). Detalle completo en la guía de S8
(3.2).

## Topics

`reserva-eventos` y `pago-eventos`, ambos con 3 particiones. `KAFKA_AUTO_CREATE_TOPICS_ENABLE`
está en `"false"` a propósito: cada topic se crea explícito, por consola
(guía de S8, 3.3) o por código (`KafkaTopicsConfig` de cada microservicio,
3.14) — nunca aparece solo la primera vez que alguien publica en un nombre
nuevo, para no ocultar un typo detrás de un topic "fantasma" con una sola
partición por defecto.

## Red compartida

```text
casa-kafka-dev-net
```

`services/casa-reserva-ms`, `services/casa-pago-ms` (por `bootstrap-servers:
localhost:19092`, la dirección `EXTERNAL`) y `uso-rapido/casa-eventos-py`
(como red *externa*, `networks: - casa-kafka-dev-net` con `external: true`)
dependen de este `compose-dev.yml`. La dependencia es dura para
`uso-rapido/casa-eventos-py`: como corre en un contenedor que se une a esta
red, Docker Compose falla con un error de red no encontrada si Kafka no está
levantado antes. Para `casa-reserva-ms`/`casa-pago-ms` en DEV es más
blanda — corren en el host y se conectan por `localhost:19092`, así que
técnicamente tolerarían arrancar primero (el cliente de Kafka reintenta) —
pero en PROD local corren como contenedores y se conectan por la dirección
`INTERNAL` (`casa-kafka:9092`), donde la dependencia vuelve a ser dura.
Por eso Kafka **siempre va primero**, en los dos ambientes: para que el
reserva sea el mismo en DEV y en PROD.

## Producción local

`compose.yml` (mismo criterio de puertos `2xxxx` que el resto del proyecto)
se une a `casa-prod-net`, creada por `infra/compose.yml`, y anuncia el
broker por el nombre del contenedor, `casa-kafka:9092`. Hoy no se levanta:
queda lista para cuando el proyecto despliegue en PROD local.
