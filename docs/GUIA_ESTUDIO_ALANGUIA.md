# Guía de estudio de Alanguia para exponer clientes seguridad y pagos

Tu objetivo es explicar cómo se identifica al usuario, dónde se guarda su perfil y cómo un evento de reserva produce un pago aprobado o una compensación. Debes poder localizar el método que aplica cada decisión, probar dos usuarios y cambiar una regla pequeña sin romper el flujo asíncrono.

Esta es la responsabilidad de estudio y exposición acordada para ti. El recorrido de propiedades y reserva se prepara en la [guía de Stephmarc](GUIA_ESTUDIO_STEPHMARC.md). Ambos deben realizar el [ensayo conjunto](EXPOSICION_Y_CAMBIOS.md).

## 1. Tu parte del código y dónde abrirla

| Tema que expones | Archivo exacto | Método o parte que debes señalar |
|---|---|---|
| Perfil del usuario | [ClienteController.java](../services/casa-cliente-ms/src/main/java/pe/edu/upeu/cliente/ClienteController.java#L16) | `me`, `guardar`, `eliminar`, `listar` y dueño del JWT |
| Datos persistidos | [Cliente.java](../services/casa-cliente-ms/src/main/java/pe/edu/upeu/cliente/Cliente.java) y [ClienteRepository.java](../services/casa-cliente-ms/src/main/java/pe/edu/upeu/cliente/ClienteRepository.java) | `usuarioId`, documento único y `findByUsuarioId` |
| Validación de perfil | [ClienteRequest.java](../services/casa-cliente-ms/src/main/java/pe/edu/upeu/cliente/ClienteRequest.java) | Record de entrada, documento, correo y teléfono |
| Consulta DNI/RUC | [DocumentoController.java](../services/casa-cliente-ms/src/main/java/pe/edu/upeu/cliente/DocumentoController.java#L11) | Validación, URL externa, token y errores 400/503/502 |
| Pantalla de perfil | [perfil.ts](../clients/casa-frontend/src/app/features/clientes/perfil.ts) | Cargar perfil, guardar y autocompletar datos |
| Seguridad en cliente | [SecurityConfig.java de cliente](../services/casa-cliente-ms/src/main/java/pe/edu/upeu/cliente/SecurityConfig.java) | Roles desde JWT y autorización de métodos |
| Login de navegador | [auth-service.ts](../clients/casa-frontend/src/app/core/auth/auth-service.ts#L46) | `iniciarLogin`, `completarLogin`, `cerrarSesion` |
| PKCE | [pkce.ts](../clients/casa-frontend/src/app/core/auth/pkce.ts) | Verificador aleatorio y hash SHA-256 |
| Envío del token | [auth-interceptor.ts](../clients/casa-frontend/src/app/core/interceptors/auth-interceptor.ts) | `Authorization: Bearer` y reacción a 401 |
| Usuarios y roles de demostración | [realm-casa.json](../keycloak/realm-casa.json) | Realm, clientes OIDC y roles ADMIN/CLIENTE |
| JWT propio de S07 | [JwtService.java](../services/casa-auth-ms/src/main/java/pe/edu/upeu/auth/service/JwtService.java) | Claims, RSA y emisor; Keycloak lo sustituye desde S07b |
| Recepción de reserva | [ReservaEventosConsumer.java](../services/casa-pago-ms/src/main/java/pe/edu/upeu/pago/messaging/ReservaEventosConsumer.java) | `@KafkaListener` y llamada a pago |
| Decisión y deduplicación | [PagoServiceImpl.procesar](../services/casa-pago-ms/src/main/java/pe/edu/upeu/pago/service/PagoServiceImpl.java#L43) | Buscar pago previo, método rechazado, guardar y preparar resultado |
| Tabla de pagos | [Pago.java](../services/casa-pago-ms/src/main/java/pe/edu/upeu/pago/entity/Pago.java) y [SQL inicial de pagos](../services/casa-pago-ms/src/main/resources/db/migration/V1__create_pagos.sql) | Reserva, dueño, monto, estado y unicidad de `reserva_id` |
| Publicación confiable | [PagoEventosProducer.java](../services/casa-pago-ms/src/main/java/pe/edu/upeu/pago/messaging/PagoEventosProducer.java#L14) | Outbox, proceso programado y confirmación del envío |
| Contrato del resultado | [PagoValidadoEvento.java](../services/casa-pago-ms/src/main/java/pe/edu/upeu/pago/event/PagoValidadoEvento.java) | Mismo contrato existe en el consumidor de reserva |
| Consulta protegida del pago | [PagoController.java](../services/casa-pago-ms/src/main/java/pe/edu/upeu/pago/controller/PagoController.java) y [PagoServiceImpl.obtener](../services/casa-pago-ms/src/main/java/pe/edu/upeu/pago/service/PagoServiceImpl.java#L90) | Consultar pago propio y listado de administrador |
| Resultado en la reserva | [PagoEventosConsumer.java de reserva](../services/casa-reserva-ms/src/main/java/pe/edu/upeu/reserva/messaging/PagoEventosConsumer.java) | `pago.validado` → marcar pagada; `pago.fallido` → compensar |
| Estados visibles | [mis-pedidos.ts](../clients/casa-frontend/src/app/features/reservas/mis-pedidos.ts#L44) y [pago-list.ts](../clients/casa-frontend/src/app/features/reservas/pago-list.ts) | Polling del cliente y listado administrativo |
| Tus pruebas principales | [PagoServiceTest.java](../services/casa-pago-ms/src/test/java/pe/edu/upeu/pago/PagoServiceTest.java) y [verificar_s07.py](../scripts/verificar_s07.py) | Duplicados, pago ajeno, rechazo y JWT propio |

## 2. Cómo explicar el servicio de clientes

Empieza por `GET /api/v1/clientes/me`. `me` no recibe un ID de usuario elegido por el navegador: usa `jwt.getSubject()` y busca el perfil mediante `findByUsuarioId()`.

En `guardar()` se recibe `ClienteRequest`, se valida y se comprueba que DNI tenga 8 dígitos o RUC 11. Si el usuario ya tiene perfil, se actualiza; si no, se crea. El controlador siempre establece `usuarioId` a partir del token.

Este módulo es pequeño y **su controlador llama directamente al repositorio**. No hay una clase `ClienteService` en esta versión. Explica la estructura que existe en pantalla, no una arquitectura que no está implementada. Como mejora posterior se podría extraer esa lógica a un servicio.

El record `ClienteRequest` describe la entrada; `Cliente` representa la tabla. Un documento repetido produce un conflicto gestionado por `Errores`. El listado completo está protegido por `@PreAuthorize` y requiere ADMIN.

`DocumentoController.consultar()` usa un proveedor para DNI/RUC. Número inválido → 400; sin token configurado → 503; fallo de proveedor → 502. La consulta real depende de `DOCUMENTOS_TOKEN`; no afirmes que se comprobó con datos oficiales si no hay esa credencial. El perfil puede llenarse manualmente.

## 3. Cómo explicar el login y los permisos

1. Angular genera un verificador PKCE y un `state` aleatorio.
2. Se dirige a Keycloak enviando el hash del verificador.
3. Keycloak autentica al usuario y devuelve un código de autorización.
4. Angular comprueba `state` y canjea el código usando el verificador.
5. El interceptor envía el JWT al llamar a las API.
6. Gateway y servicios validan el token y aplican sus permisos. El dueño del registro se controla en el servicio del dato.

Señala `iniciarLogin()` y `completarLogin()` en el código. La función que decodifica un JWT en el navegador no verifica su firma: sirve para la interfaz. El servidor valida el JWT como resource server.

No confundas cuentas de la aplicación con el administrador de la consola Keycloak. Tampoco confundas S07 con S07b: el servidor JWT propio es una práctica conservada; el login final usa Keycloak. Para el usuario no hay dos autenticaciones obligatorias.

## 4. Cómo explicar el pago asíncrono

Empieza donde termina la exposición de Stephmarc: existe una reserva `PENDIENTE_PAGO` y un evento pendiente en la base de reserva.

El despachador publica `reserva.creada` en `reserva-eventos`. `ReservaEventosConsumer` lo recibe en pago y llama a `procesar()`. Pago consulta si ya hay un pago para esa reserva. Si lo hay, ignora el duplicado. Si no, decide si el método es `TARJETA_RECHAZADA`: en ese caso guarda `FALLIDO`; en los demás, `VALIDADO`.

El resultado se registra en el outbox de pago. `PagoEventosProducer.despachar()` intenta enviarlo a `pago-eventos`. El consumidor de reserva llama a `marcarPagada()` o `compensar()`.

| Concepto | Qué mostrar para demostrarlo |
|---|---|
| Asincronía | La creación devuelve pendiente antes de recibir el resultado del pago. |
| Outbox | `publicarTrasCommit()` guarda un `EventoPendiente`; `despachar()` envía y elimina tras confirmación. |
| Idempotencia | Comprobación previa y unicidad de `reserva_id`; dos entregas no deben crear dos pagos. |
| Compensación | Pago rechazado → devolver disponibilidad y cancelar reserva. |
| Consistencia eventual | El frontend consulta pendientes y muestra el estado final después. |

El nombre `PagoValidadoEvento` se usa para resultados aprobados **y rechazados**; el campo `tipoEvento` los distingue. Para modificar el contrato debes revisar tanto el productor como su clase equivalente en reserva. No cambies un topic o tipo de evento en un solo lado.

## 5. Cambios que debes practicar

Son ejercicios en una rama de ensayo, no funciones nuevas ya aplicadas.

### Cambio A Aceptar solo teléfono vacío o de nueve dígitos

**Archivo:** `ClienteRequest.java`, componente `telefono`. Conserva que sea opcional y añade:

```java
@Pattern(regexp = "^$|[0-9]{9}", message = "El teléfono debe tener nueve dígitos")
```

Puedes conservar `@Size(max = 30)` aunque la nueva regla sea más estricta. En `perfil.ts`, añade al input de teléfono `pattern="[0-9]{9}"` y `maxlength="9"`. Si el docente lo quiere obligatorio, también necesitas `@NotBlank` y `required`; es una regla distinta.

**Demostración:** guarda un perfil con teléfono válido y luego el mismo request con `123` o `abc`. Con la nueva regla, deben responder 200 y 400 respectivamente. Mantén un documento propio para que la prueba no falle por unicidad.

### Cambio B Simular también una transferencia rechazada

**Archivo principal:** `PagoServiceImpl.java`, cálculo de `rechazado` en `procesar()`.

Amplía la condición con OR, conservando la tarjeta rechazada:

```java
boolean rechazado = METODO_RECHAZADO.equalsIgnoreCase(reserva.getMetodoPago())
    || "TRANSFERENCIA_RECHAZADA".equalsIgnoreCase(reserva.getMetodoPago());
```

Coordina con Stephmarc una nueva opción de demostración en `reserva-crear.html`. Añade una prueba al módulo de pago para el nuevo método. No basta con cambiar la etiqueta del select: el servidor debe aplicar el rechazo.

**Demostración:** usa una propiedad disponible, crea la reserva con el nuevo método, espera `CANCELADA` y confirma que la disponibilidad volvió a 1. Prueba también `TARJETA`: debe seguir aprobándose. El pago se simula; no se realiza una transferencia bancaria real.

### Cambio C Hacer más visible la espera del pago

**Archivos compartidos:** `PagoEventosProducer.java` y `ReservaEventosProducer.java`, `@Scheduled(fixedDelay=2000)`.

Para ensayar una espera mayor, cambia ambos a `fixedDelay=5000`. Reconstruye ambos servicios. Explica que cambiaste cada intervalo de despacho, no una garantía exacta de respuesta en cinco segundos: también influyen el broker, el consumidor y el polling de Angular.

Observa pendiente → estado final y comprueba que el pago no se duplica. No añadas `sleep` dentro de una transacción para fingir demora.

### Cambio D Cambiar un texto del perfil sin tocar la lógica

**Archivo:** `perfil.ts`, plantilla y mensajes. Cambia «Guardar perfil» por «Actualizar mis datos» y un mensaje de éxito coherente.

No cambies la ruta `/api/v1/clientes/me` solo para modificar un texto. Comprueba que se sigue enviando PUT y que el perfil continúa perteneciendo al mismo JWT. Es un cambio solo de frontend.

### Cambio avanzado Agregar una observación al perfil

Piezas a revisar: `Cliente`, `ClienteRequest`, copia de datos en `ClienteController.guardar()`, nueva migración SQL y formulario de `perfil.ts`. La entidad se devuelve directamente en esta versión; no hay un `ClienteResponse` separado que debas inventar como si ya existiera.

Ejemplo de migración nueva si V2 está libre: añadir `observacion VARCHAR(200)`. Usa `@Size(max=200)` para la entrada y contempla valores nulos para perfiles existentes. Comprueba crear, leer y actualizar. Si después decides introducir un DTO de salida, eso sería una refactorización adicional.

## 6. Cómo poner en funcionamiento tus cambios

Desde la raíz del proyecto, para el servicio de clientes:

```powershell
mvn -f services/casa-cliente-ms/pom.xml package
$env:CASA_PROFILE = 'prod'
docker compose -f compose.json build cliente
docker compose -f compose.json up -d --no-deps cliente
docker compose -f compose.json logs --tail 30 cliente
```

Para pago:

```powershell
mvn -f services/casa-pago-ms/pom.xml package
$env:CASA_PROFILE = 'prod'
docker compose -f compose.json build pago
docker compose -f compose.json up -d --no-deps pago
```

Si cambias ambos despachadores de outbox, compila y reconstruye **reserva y pago**. Para texto o formulario usa el [procedimiento de Angular](EXPOSICION_Y_CAMBIOS.md#cambios-en-angular). Espera a que el servicio vuelva a estar disponible antes de concluir que la prueba falló.

## 7. Preguntas que podrían hacerte

| Pregunta | Respuesta breve y lugar que debes mostrar |
|---|---|
| ¿Quién elige a qué cliente pertenece el perfil? | El servidor usa `jwt.getSubject()` en `ClienteController`; no lo decide el request. |
| ¿Qué aporta PKCE? | Vincula el canje del código al cliente que inició el login; muestra verificador y challenge. |
| ¿Ocultar botones protege la API? | No. La autorización real se aplica en Gateway y servicios. |
| ¿Por qué un pago ajeno devuelve 404? | Se filtra por dueño y no se entrega el recurso de otro usuario. |
| ¿Kafka garantiza exactamente una entrega? | No en este diseño; por eso existen deduplicación y controles de estado. |
| ¿Qué se guarda primero: pago o evento? | Ambos se guardan en la transacción local mediante outbox; el envío al broker ocurre aparte. |
| ¿Qué evita dos pagos simultáneos para la misma reserva? | La comprobación de negocio ayuda y la restricción única de base protege frente a carreras. |
| ¿Qué pasa si Kafka cae? | El evento pendiente permanece para reintento; la prueba de fallos muestra su recuperación. |
| ¿Qué significa un 503 en consulta DNI? | Puede faltar el token configurado; revisa el mensaje y no lo presentes como documento inexistente. |
| ¿Editar el JSON del realm cambia un usuario existente? | No automáticamente: el import inicial no actualiza de esa forma un realm ya creado. |

## 8. Tu ensayo antes de exponer

Primera pasada: recorre perfil → JWT → reserva → evento → pago → resultado en reserva. Segunda pasada: muestra un pago aprobado y otro rechazado e identifica en Kafka qué evento produjo cada estado. Tercera pasada: practica el cambio A o B y prueba un caso que debe pasar y uno que debe fallar. Finalmente explica una clase de reserva a Stephmarc y pídele que te explique un consumidor.

Puedes iniciar tu intervención así: «Desde aquí el usuario ya está identificado y la reserva está pendiente. Voy a mostrar cómo llega el evento a pago, dónde decidimos el resultado y cómo evitamos repetir el efecto si el mensaje vuelve a llegar». Después abre los métodos reales y acompaña cada afirmación con el código o una respuesta de la API.
