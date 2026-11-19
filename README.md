# CASA O NADA

**Para estudiar el código:** [Guía paso a paso, recorrido de una reserva y preguntas de defensa](docs/GUIA_ESTUDIO.md).

**Para la exposición de cada integrante:** [Guía de Stephmarc](docs/GUIA_ESTUDIO_STEPHMARC.md) · [Guía de Alanguia](docs/GUIA_ESTUDIO_ALANGUIA.md) · [Ensayo conjunto y cambios en vivo](docs/EXPOSICION_Y_CAMBIOS.md).

El catálogo incluye fotografías reales de referencia por categoría. [Fuentes y créditos](docs/CREDITOS_FOTOS.md).

Proyecto académico de reservas inmobiliarias para Desarrollo de Aplicaciones Distribuidas, adaptado de las guías S01–S16 de Angel Sullon Macalupu (UPeU). Incluye S07b. La implementación parte del repositorio público del docente; ver `docs/ORIGEN.md`.

## Arranque

Requisitos: Java 21, Maven 3.9+, Docker Desktop con motor Linux, conexión para descargar imágenes/dependencias. Recomendado: 12–16 GB de RAM disponibles para el conjunto completo.

Desde esta carpeta, en PowerShell:

```powershell
.\scripts\iniciar.ps1
python scripts/verificar.py
```

El script inicia el perfil `prod` de producción local. Para prácticas con Swagger habilitado usa `.\scripts\iniciar.ps1 -Perfil dev`. Ambos perfiles se verifican con el mismo script E2E.

El arranque compila y ejecuta pruebas antes de construir las imágenes. La primera descarga tarda varios minutos. El frontend se compila dentro de Docker con Node 24. Para desarrollo manual Angular requiere Node >=24.15.0 (o la versión compatible indicada por Angular CLI).

```powershell
mvn clean package
docker compose -f compose.json up -d --build
docker compose -f compose.json ps
docker compose -f compose.json logs --tail 100 reserva
```

No se necesita configurar manualmente Keycloak: el realm y los usuarios se importan al primer arranque. Las migraciones Flyway crean las tablas y los datos inmobiliarios.

| Pantalla | Dirección | Acceso local de demostración |
|---|---|---|
| Aplicación | http://localhost:4200 | admin, cliente1 o cliente2 / `Casa2026!` |
| Gateway | http://localhost:18080 | API |
| Eureka | http://localhost:18761 | Registro de servicios |
| Config Server | http://localhost:18888 | `/casa-reserva-ms/dev` |
| Keycloak | http://localhost:18180 | administrador de Keycloak: admin / `AdminLocal2026!` |
| Grafana | http://localhost:13000 | admin / `Casa2026!` |
| Prometheus | http://localhost:19090 | Métricas y alertas |
| Kafka UI | http://localhost:18085 | Topics y eventos |
| Loki | http://localhost:13100 | Consultas de logs |

Las credenciales son exclusivamente para producción local académica. Los puertos publicados escuchan en loopback. Copia `.env.example` a `.env` para cambiar el secreto interno o configurar `DOCUMENTOS_TOKEN`. No publiques `.env` ni tokens.

## Qué hace

El administrador mantiene tipos y propiedades con dirección, ciudad, área y dormitorios. Cada propiedad tiene un único cupo disponible. El cliente entra por Keycloak con Authorization Code + PKCE, registra su perfil, selecciona una propiedad y solicita la reserva. El precio representa la separación, no el valor de compraventa del inmueble.

Reserva consulta propiedad por Feign con Eureka y Circuit Breaker, bloquea la disponibilidad, persiste la reserva y un evento de outbox en la misma transacción. El despachador envía a Kafka con reintento. Pago simula aprobación o rechazo; emite su resultado mediante otro outbox. Reserva termina PAGADA o CANCELADA y, en rechazo, devuelve el cupo. El cliente ve el resultado mediante polling.

Los pagos son simulados, sin cobros reales. `TARJETA_RECHAZADA` demuestra la compensación. RENIEC/SUNAT se consulta mediante un proveedor externo configurado por token; sin token se permite registrar el perfil manualmente y la consulta responde 503. No se inventan respuestas oficiales.

## Código y evidencia

- `services/`: propiedad, reserva, pago, cliente y auth (histórico S07).
- `infra/`: configuración centralizada, registro y Gateway.
- `clients/casa-frontend/`: Angular y PKCE.
- `compose.json`: despliegue integrado. Los compose por carpeta son referencias heredadas para las prácticas; usa el compose raíz para la entrega integrada.
- `obs/`: dashboard, logs y alertas del despliegue raíz.
- `docs/MATRIZ_SESIONES.md`: cobertura sesión por sesión.
- `docs/ARQUITECTURA.md`: contratos y flujo.
- `docs/REPARTO_Y_DEFENSA.md`: propuesta equilibrada y preguntas de sustentación.
- `evidencias/`: resultados reales de ejecución, sin contraseñas ni JWT.
- `docs/guias-docente/`: copia de las guías originales para revisar rúbricas y entregas individuales.

No se atribuyen commits, capturas ni contribuciones personales que no ocurrieron. Las sustentaciones, las reflexiones y las capturas individuales con reloj y perfil deben realizarlas los integrantes según las rúbricas.

## Detener

```powershell
.\scripts\detener.ps1
```

Conserva las bases de datos. El inicio posterior mantiene tus datos. Los usuarios del realm se importan al crear el realm; editar el JSON no modifica un realm existente.

## S07 antes de Keycloak

```powershell
docker compose -f compose.json --profile s07 up -d --build auth
```

Auth queda en http://localhost:18107 con registro/login propios y `/.well-known/jwks.json`; firma RSA y roles en PostgreSQL. Para verificar el cambio de emisor, configura tanto `SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI=http://localhost:18107` como `SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_JWK_SET_URI=http://auth:8080/.well-known/jwks.json` en los resource servers y recréalos. El frontend final usa Keycloak; la práctica S07 se demuestra por API.

## Pruebas de recuperación

`python scripts/verificar_fallos.py` detiene y restaura exclusivamente Kafka y las dos instancias de propiedad del proyecto. Ejecutarlo sin demostraciones simultáneas: simula interrupciones reales y deja resultados en `evidencias/fallos.json`.

En S07, las claves del servidor JWT propio se generan al arrancar: reiniciarlo invalida sus tokens anteriores. El sistema final usa Keycloak para administrar la identidad.

## Verificaciones adicionales

- `python scripts/verificar_infra.py`: balanceo, perfiles y métricas.
- `python scripts/verificar_s07.py`: requiere iniciar antes el perfil opcional `s07`.
- `ESTADO_VERIFICADO.md`: resultados y alcance real de la entrega.
