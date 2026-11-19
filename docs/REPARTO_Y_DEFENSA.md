# Reparto propuesto y sustentación

Esta es una propuesta para estudiar, revisar y asumir responsabilidades; no afirma que los integrantes ya hayan realizado estos aportes.

| Integrante | Responsabilidad principal | Verificación y explicación |
|---|---|---|
| Tú | Propiedad, tipos, Flyway, Feign y reservas | CRUD, bloqueo concurrente, precio del servidor y circuito abierto |
| Alanguia | Cliente, auth/Keycloak, Kafka y pago | Perfil y DNI/RUC, PKCE, dueños, duplicados y compensación |
| Ambos | Config, Eureka, Gateway, Angular, observabilidad, E2E y documentación | Cada uno ejecuta y explica la mitad del recorrido, luego intercambian responsabilidades |

Para equilibrar la dificultad, tú preparas las pruebas de concurrencia/aislamiento y Alanguia las de mensajería/compensación. Ambos revisan el código del otro. Registrar en Git únicamente los cambios que efectivamente hagan, con su autor real.

## Demostración de 12 minutos

1. Mostrar arquitectura, bases por servicio y el arranque reproducible (ambos, 1 minuto).
2. Eureka con dos instancias y configuración centralizada (tú, 1 minuto).
3. Administrador crea una propiedad; explicar DTO, validación y migraciones (tú, 2 minutos).
4. Cliente inicia sesión por PKCE, registra su perfil y reserva (Alanguia, 2 minutos).
5. Mostrar topic y pago: PENDIENTE_PAGO → PAGADA (Alanguia, 1 minuto).
6. Rechazar otro pago: CANCELADA y propiedad disponible; explicar outbox e idempotencia (Alanguia, 2 minutos).
7. Consultar la reserva como otro usuario: 404. Negar mutación interna y comprobar login JWT (tú, 1 minuto).
8. Buscar la traza en Grafana y mostrar los resultados de pruebas (ambos, 2 minutos).

## Preguntas que ambos deben responder

- ¿Por qué Gateway valida la ruta y el servicio valida el dueño? La ruta no conoce al dueño del registro; el servicio lo compara con `sub`.
- ¿Por qué se usa 404 para un objeto ajeno? Evita confirmar su existencia y no entrega información de otro cliente.
- ¿Qué ocurre si dos clientes reservan a la vez? La fila se bloquea al modificar su única disponibilidad; solo uno consume el cupo.
- ¿Qué aporta Feign frente a Kafka? Feign obtiene una respuesta inmediata del catálogo; Kafka desacopla el resultado eventual del pago.
- ¿Qué ocurre si Kafka falla tras guardar la reserva? El evento permanece en outbox en la misma base y se reenvía al recuperar el broker.
- ¿Por qué pueden llegar eventos duplicados? El envío es al menos una vez; la restricción por reserva y el control del estado evitan efectos repetidos.
- ¿Qué pasa si la compensación no puede consultar propiedad? No se marca cancelada silenciosamente; se conserva el error para reintento.
- ¿Qué protege PKCE? Vincula el código de autorización al cliente que inició el login mediante un verificador secreto temporal.
- ¿Dónde se obtiene el precio y el cliente? El precio del servicio propiedad; el cliente del JWT validado, nunca del request.
- ¿Por qué no marcar S15/S16 como aprobadas? La evaluación y la defensa pertenecen a los estudiantes y al docente.

## Práctica individual

Cada integrante ejecuta los comandos desde cero, dibuja el flujo sin leer, explica una clase del compañero y repite una prueba fallida real después de corregirla. Completar nombre, equipo, enlace al repositorio y reflexiones en los informes que pida cada guía.
