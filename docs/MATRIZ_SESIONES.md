# Matriz S01–S16

La cobertura de implementación se separa de la evaluación personal. Los resultados ejecutados están en `../evidencias`; una guía o un archivo de código por sí solos no demuestran una prueba.

| Sesión | Cobertura | Ubicación |
|---|---|---|
| S01 | CRUD propiedad/tipo, PostgreSQL, Flyway, validación, Swagger y health | services/casa-propiedad-ms |
| S02 | Config Server, perfiles dev/prod y cliente autónomo | infra/casa-config; services/casa-cliente-ms |
| S03 | Eureka y dos instancias de propiedad | compose.json: propiedad/propiedad2; infra/casa-eureka |
| S04 | Gateway por Eureka, balanceo, métricas y logs | infra/casa-gateway; obs |
| S05 | Evaluación U1: recorrido técnico y evidencias del sistema base | scripts/verificar.py; REPARTO_Y_DEFENSA.md |
| S06 | Feign + Circuit Breaker y fallback sin pago | services/casa-reserva-ms |
| S07 | JWT RSA, JWKS, contraseñas cifradas, roles propios | services/casa-auth-ms; perfil compose s07 |
| S07b | Keycloak, realm, roles y sustitución de emisor | keycloak/realm-casa.json |
| S08 | Reserva creada → pago validado por Kafka | services/casa-reserva-ms; services/casa-pago-ms |
| S09 | Rechazo, compensación, idempotencia y outbox | migraciones V4; mensajería; ReservaServiceTest |
| S10 | Prometheus, Loki, Grafana, métricas Keycloak y alertas | obs; compose.json |
| S11 | Angular, PKCE, autorización, reserva y polling | clients/casa-frontend |
| S12 | Evaluación U2: demostrar integración robusta | scripts/verificar.py; guía de defensa |
| S13 | E2E con dos usuarios, aislamiento y correlación | scripts/verificar.py; evidencias/integracion.json |
| S14 | Arranque reproducible, documentación y estabilización | README; scripts; compose.json |
| S15 | Evaluación grupal del producto final | REPARTO_Y_DEFENSA.md; rúbrica original |
| S16 | Defensa individual y revisión de aportes | REPARTO_Y_DEFENSA.md; rúbrica original |

## Evidencia individual pendiente de los estudiantes

S05, S12, S15 y S16 son evaluaciones. El repositorio aporta sistema y guion; no sustituye la defensa individual. Las guías originales conservan las rúbricas completas. Para informes por sesión, capturar la ejecución con reloj y perfil visibles, registrar un problema real, responder la reflexión y feedback con las palabras de cada integrante. No fabricar historial de commits ni imágenes.
