# Bitácora de estabilización

Los hallazgos siguientes ocurrieron durante esta implementación. No son aportes atribuidos a los estudiantes.

| Hallazgo real | Causa y corrección | Verificación |
|---|---|---|
| Propiedad no arrancaba con Hibernate | Durante la adaptación, el nombre SQL del identificador de tipo no coincidía con el nombre físico JPA. Se agregó V4 para renombrar la columna conservando los datos. | Las dos instancias arrancan; CRUD y concurrencia pasan por Gateway. |
| Angular no compilaba inicialmente | La sustitución de nombres produjo un guion dentro de un identificador TypeScript. Se separaron los identificadores de código de las rutas HTTP. | Compilación de Angular terminada sin errores. |
| Node instalado no era compatible con Angular 22 | La versión local era 24.11 y CLI requiere 24.15 o posterior dentro de esa rama. Se usó una instalación local de Node 24.15 para validar; el Dockerfile usa Node 24 actualizado. | Build y tres pruebas de PKCE pasan. No se modificó Node global. |
| Perfil no mostraba los datos recibidos | En Angular sin Zone.js, asignar un campo ordinario dentro del callback HTTP no actualizaba la pantalla. El resultado ahora actualiza el estado reactivo de la pantalla. | Revisión visual: `Perfil cargado` y campos del usuario de prueba visibles. |
| Compensación podía ocultar un fallo de restauración | El fallback original solo registraba el error. Ahora lo propaga y mantiene pendiente la transición para reintento. | Prueba de comportamiento `compensacionFallidaPermanecePendiente`. |
| Disponibilidad vulnerable a solicitudes simultáneas | El ejemplo consultaba y modificaba sin bloqueo. Se aplica bloqueo pesimista y un único cupo por inmueble. | Dos usuarios compiten por la misma propiedad: solo una reserva confirmada. |
| Eventos podían perderse entre commit y publicación | La base original publicaba después del commit. Se agregó outbox persistente a reserva y pago. | Kafka se detiene, se crea la reserva y, al volver, el pago termina procesado. |

La revisión automática rechazó una propuesta de borrar el esquema de prueba. No se realizó ese borrado: la corrección definitiva usa una migración que conserva datos.

Las pruebas de API crean datos con nombres de prueba, que permanecen en el entorno local. Los archivos de evidencia no incluyen JWT ni tokens del proveedor externo.
