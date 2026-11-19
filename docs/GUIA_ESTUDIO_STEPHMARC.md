# Guía de estudio de Stephmarc para exponer propiedades y reservas

Tu objetivo es explicar cómo una propiedad pasa del formulario a la base de datos y cómo un cliente ocupa su disponibilidad mediante una reserva. Debes poder abrir la clase que contiene cada regla y modificarla sin romper el contrato con el frontend ni con los otros servicios.

Esta guía asigna responsabilidades de estudio y exposición. La entrega inicial se subió completa; el reparto no cambia la autoría histórica de sus commits. El flujo compartido y el trabajo de Alanguia están en [su guía](GUIA_ESTUDIO_ALANGUIA.md) y en el [ensayo conjunto](EXPOSICION_Y_CAMBIOS.md).

## 1. Tu parte del código y dónde abrirla

Los enlaces llevan a los archivos del repositorio y, cuando se indica un método, a su línea actual en GitHub. En VS Code puedes usar Ctrl+P con el nombre de la clase y Ctrl+F para localizar el método.

| Tema que expones | Archivo exacto | Método o parte que debes señalar |
|---|---|---|
| API de propiedades | [PropiedadController.java](../services/casa-propiedad-ms/src/main/java/pe/edu/upeu/propiedad/controller/PropiedadController.java#L34) | `crear`, `actualizar`, `listar`, rutas HTTP y `@Valid` |
| Reglas de propiedades | [PropiedadService.java](../services/casa-propiedad-ms/src/main/java/pe/edu/upeu/propiedad/service/PropiedadService.java#L36) | Crear, actualizar y buscar el tipo válido |
| Validación de entrada | [PropiedadRequest.java](../services/casa-propiedad-ms/src/main/java/pe/edu/upeu/propiedad/dto/PropiedadRequest.java) | Nombre, precio, área, dormitorios, disponibilidad y tipo |
| Tabla y relación | [Propiedad.java](../services/casa-propiedad-ms/src/main/java/pe/edu/upeu/propiedad/entity/Propiedad.java) | ID, columnas, `@ManyToOne`, `@Version` |
| Conversión de datos | [PropiedadMapper.java](../services/casa-propiedad-ms/src/main/java/pe/edu/upeu/propiedad/mapper/PropiedadMapper.java) | Entidad ↔ DTO; código generado al compilar |
| Consulta y concurrencia | [PropiedadRepository.java](../services/casa-propiedad-ms/src/main/java/pe/edu/upeu/propiedad/repository/PropiedadRepository.java#L13) | `bloquear`, bloqueo pesimista y consulta con el tipo |
| Tipos de propiedad | [TipoPropiedadController.java](../services/casa-propiedad-ms/src/main/java/pe/edu/upeu/propiedad/controller/TipoPropiedadController.java) y [TipoPropiedadService.java](../services/casa-propiedad-ms/src/main/java/pe/edu/upeu/propiedad/service/TipoPropiedadService.java) | Segundo CRUD y validación del tipo referenciado |
| Cupo de la propiedad | [PropiedadService.descontarStock](../services/casa-propiedad-ms/src/main/java/pe/edu/upeu/propiedad/service/PropiedadService.java#L72) | Cantidad 1, publicada, disponible y bloqueo de fila |
| API de reservas | [ReservaController.java](../services/casa-reserva-ms/src/main/java/pe/edu/upeu/reserva/controller/ReservaController.java#L23) | Dueño obtenido del JWT con `getSubject()` |
| Regla de una propiedad por reserva | [ReservaRequest.java](../services/casa-reserva-ms/src/main/java/pe/edu/upeu/reserva/dto/ReservaRequest.java) y [DetalleReservaRequest.java](../services/casa-reserva-ms/src/main/java/pe/edu/upeu/reserva/dto/DetalleReservaRequest.java) | Un detalle, cantidad positiva y máximo 1 |
| Caso de uso principal | [ReservaServiceImpl.crear](../services/casa-reserva-ms/src/main/java/pe/edu/upeu/reserva/service/ReservaServiceImpl.java#L32) | Consultar propiedad, ocupar cupo, calcular precio, guardar y preparar evento |
| HTTP entre servicios | [PropiedadClient.java](../services/casa-reserva-ms/src/main/java/pe/edu/upeu/reserva/client/PropiedadClient.java) y [PropiedadConsultaService.java](../services/casa-reserva-ms/src/main/java/pe/edu/upeu/reserva/service/PropiedadConsultaService.java) | Feign, descubrimiento por nombre y Circuit Breaker |
| Datos propios del cliente | [ReservaServiceImpl.findById](../services/casa-reserva-ms/src/main/java/pe/edu/upeu/reserva/service/ReservaServiceImpl.java#L108) | Filtro por dueño antes de devolver el DTO |
| Resultado del pago y devolución del cupo | [marcarPagada](../services/casa-reserva-ms/src/main/java/pe/edu/upeu/reserva/service/ReservaServiceImpl.java#L96) y [compensar](../services/casa-reserva-ms/src/main/java/pe/edu/upeu/reserva/service/ReservaServiceImpl.java#L148) | Estado previo, bloqueo y compensación; coordinar explicación con Alanguia |
| Catálogo y formulario visual | [propiedades.ts](../clients/casa-frontend/src/app/features/propiedad/propiedades.ts) | `cargar`, `filtradas`, `guardar`, campos del formulario |
| Selección y envío de reserva | [reserva-crear.ts](../clients/casa-frontend/src/app/features/reservas/reserva-crear.ts#L58) y [reserva-crear.html](../clients/casa-frontend/src/app/features/reservas/reserva-crear.html) | Construcción del request y estados de carga |
| Migraciones | [Carpeta SQL de propiedad](../services/casa-propiedad-ms/src/main/resources/db/migration) | Tablas, semillas, campos inmobiliarios y normalización de columna |
| Tus pruebas principales | [DisponibilidadTest.java](../services/casa-propiedad-ms/src/test/java/pe/edu/upeu/propiedad/DisponibilidadTest.java) y [ReservaServiceTest.java](../services/casa-reserva-ms/src/test/java/pe/edu/upeu/reserva/ReservaServiceTest.java) | Cantidad inválida, propiedad ocupada, dueño, compensación y estados |

## 2. Cómo explicar un CRUD sin memorizar un discurso

Abre `PropiedadController.crear()` y di: «Esta ruta recibe el JSON. `@Valid` activa las reglas de `PropiedadRequest`. El controlador delega la lógica en el servicio».

Pasa a `PropiedadService.crear()`: el mapper convierte el request a entidad; el servicio busca un tipo existente; el repositorio guarda la entidad y se devuelve un response. JPA se encarga de persistirla en PostgreSQL.

Señala la diferencia entre crear y actualizar: `actualizar()` busca un registro existente y copia explícitamente sus campos. Por eso, al añadir un campo nuevo, no basta con que MapStruct lo copie al crear: también debes revisar la actualización manual.

La entidad representa persistencia; el DTO define el contrato de la API. `@Builder`, getters y setters vienen de Lombok. El código generado por Lombok o MapStruct evita repetición, pero las reglas de disponibilidad están escritas en el servicio.

## 3. Cómo explicar la reserva paso a paso

1. Angular manda `metodoPago` y un detalle con `idPropiedad` y `cantidad = 1`. No manda un total fiable ni el dueño.
2. `ReservaController` toma el dueño del `sub` del JWT validado.
3. `ReservaServiceImpl.crear()` consulta la propiedad mediante `PropiedadConsultaService` y Feign.
4. La llamada para ocupar disponibilidad entra en propiedad. `descontarStock()` exige cantidad 1, comprueba `activo`, bloquea la fila y cambia el cupo de 1 a 0.
5. Reserva calcula el importe usando el precio recibido del servidor. Si todo fue válido, queda `PENDIENTE_PAGO`; si no, queda `CARRITO`, sin total ni evento de pago.
6. Reserva guarda su evento en outbox. A partir de aquí Alanguia explica Kafka y pago.
7. Cuando llega el resultado, `marcarPagada()` cambia a `PAGADA`; `compensar()` intenta devolver el cupo y cambia a `CANCELADA`.

No digas que hay una sola transacción entre ambos servicios. El bloqueo protege la fila dentro de propiedad; la transacción de reserva protege su propia base. La ventana entre la llamada HTTP y confirmar la reserva sigue siendo una mejora pendiente de coordinación distribuida.

## 4. Cambios que debes practicar

Estos son ejercicios para una rama de ensayo; no están aplicados al proyecto entregado.

### Cambio A Hacer que la reserva cueste al menos 100 soles

**Archivos:** `PropiedadRequest.java`, campo `precio`, y `propiedades.ts`, input `name="precio"`.

El backend actualmente usa `@DecimalMin(value = "0.0", inclusive = true)`. Para el ejercicio cambia el mínimo a `100.00`. En Angular, cambia el `min` de ese input a `100`; no cambies los mínimos de área o dormitorios.

**Por qué dos lugares:** el formulario orienta al usuario; el DTO protege la API aunque la llamen sin navegador. La tabla no cambia porque el campo ya existe.

**Demostración:** manda una propiedad con todos los demás campos válidos y precio `99.99`: debe responder 400. Con `100.00` debe responder 201. Usa el mismo request y modifica solo el precio para que el resultado demuestre esa validación, no otro error.

### Cambio B Buscar también por dirección

**Archivo:** `propiedades.ts`, método `filtradas()`.

Actualmente concatena nombre y ciudad. Amplía el texto buscable con dirección:

```typescript
(p.nombre + ' ' + p.ciudad + ' ' + (p.direccion ?? ''))
  .toLowerCase()
  .includes(this.busqueda.toLowerCase())
```

Actualiza el texto del buscador para mencionar dirección. Prueba una palabra presente solo en la dirección y luego una búsqueda vacía. Este cambio es de interfaz; no necesita migración ni reconstruir Java. El catálogo ya estaba cargado en el navegador: no estás implementando una búsqueda paginada de servidor.

### Cambio C Exigir una ciudad de máximo 80 caracteres

**Archivo:** `PropiedadRequest.java`, campo `ciudad`; añade `@Size(max = 80)` y un mensaje. En el formulario añade `maxlength="80"` al input de ciudad.

La entidad ya limita ciudad a 80; verifica ese dato en `Propiedad.java`. Aquí se busca rechazar la entrada antes de llegar al error de base, no modificar la columna. Prueba 80 y 81 caracteres con el resto del request válido.

### Cambio D Cambiar lo que ve el cliente para una reserva pagada

**Archivo compartido:** [mis-pedidos.ts](../clients/casa-frontend/src/app/features/reservas/mis-pedidos.ts#L71), método `etiqueta()`.

Cambia el texto de presentación de `PAGADA` a «Reserva confirmada». Mantén el valor de estado `PAGADA` en Java, en los eventos y en la base. Explica al docente la diferencia entre cambiar una etiqueta visual y cambiar un contrato del sistema.

### Cambio avanzado Agregar estacionamientos

Antes de escribir, enumera las piezas: entidad `Propiedad`, request, response, mapper, copia manual en `PropiedadService.actualizar()`, formulario y tarjeta Angular. Añade una migración nueva, por ejemplo `V5__estacionamientos.sql`, con `estacionamientos INTEGER NOT NULL DEFAULT 0`, siempre comprobando que V5 siga libre.

Usa `@NotNull` y `@Min(0)` en el request. Los registros existentes requieren valor por defecto. Comprueba crear, consultar y actualizar. Revisa también los scripts que crean propiedades de prueba: si el nuevo campo es obligatorio, sus payloads deben enviarlo. Este ejercicio cruza varias capas y conviene ensayarlo antes de intentar hacerlo en pocos minutos.

## 5. Cómo poner en funcionamiento tus cambios

Ejecuta desde la raíz donde están `pom.xml` y `compose.json`. Para cambiar solo propiedad:

```powershell
mvn -f services/casa-propiedad-ms/pom.xml package
$env:CASA_PROFILE = 'prod'
docker compose -f compose.json build propiedad propiedad2
docker compose -f compose.json up -d --no-deps propiedad propiedad2
docker compose -f compose.json logs --tail 30 propiedad
```

Hay dos instancias: si actualizas una sola, Gateway podría atender algunas solicitudes con la versión antigua. `package` ya ejecuta las pruebas del módulo. Si falla, corrige antes de reconstruir Docker.

Para modificar la lógica de reservas:

```powershell
mvn -f services/casa-reserva-ms/pom.xml package
$env:CASA_PROFILE = 'prod'
docker compose -f compose.json build reserva
docker compose -f compose.json up -d --no-deps reserva
```

Para los cambios del formulario o del buscador, usa el [procedimiento de frontend](EXPOSICION_Y_CAMBIOS.md#cambios-en-angular). El nombre del servicio Docker es `reserva`, no la ruta `services/casa-reserva-ms`.

## 6. Preguntas que podrían hacerte

| Pregunta | Respuesta que debes demostrar en el archivo |
|---|---|
| ¿Dónde se impide reservar dos propiedades? | `ReservaRequest` limita la lista a un detalle y `DetalleReservaRequest` limita cantidad a 1. |
| ¿Dónde se evita que dos usuarios ocupen el mismo cupo? | `PropiedadRepository.bloquear()` y la transacción de `descontarStock()`. |
| ¿El precio viene del usuario? | No; `crear()` lo toma de `PropiedadDto`, devuelto por propiedad. |
| ¿Por qué se valida un tipo antes de guardar? | La propiedad debe referenciar un tipo existente; el servicio lo busca y falla si no existe. |
| ¿Qué ocurre si propiedad cae? | El fallback evita confirmar la reserva; queda `CARRITO` y no inicia pago. |
| ¿Por qué un usuario no puede leer otra reserva cambiando el ID? | `findById()` compara `idCliente` con el dueño autenticado y entrega 404 si no coincide. |
| ¿Qué pasa si falla devolver la disponibilidad? | El fallback propaga el error; no se marca cancelada silenciosamente y se permite reintento. |
| ¿Cambiar un DTO cambia automáticamente la base? | No; la persistencia está en la entidad y las migraciones. |

## 7. Tu ensayo antes de exponer

Primera pasada: abre los archivos de las primeras doce filas de la tabla y sigue una reserva con el dedo, sin modificar nada. Segunda pasada: explica el recorrido en cinco minutos y ejecuta un CRUD. Tercera pasada: practica los cambios A y B en una rama, comprobando el caso válido y el inválido. Por último, pídele a Alanguia que te indique una clase al azar y explica qué entra, qué sale y qué regla aplica.

Debes poder terminar estas frases: «esta validación está aquí porque…», «esta clase llama a…», «si esto falla ocurre…», «para demostrar el cambio probaré…». Si solo puedes leer el nombre de la anotación, vuelve al ejemplo de ejecución.
