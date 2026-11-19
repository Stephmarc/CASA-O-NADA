# Preparación de la exposición de código y de los cambios en vivo

Estudien primero su recorrido principal y luego ensayen una modificación pequeña con su verificación. La meta es poder señalar dónde se aplica una regla, cambiarla en las capas necesarias y mostrar que funciona sin romper el flujo anterior.

## Guías de cada integrante

| Integrante | Parte principal de la exposición | Guía con archivos métodos y ejercicios |
|---|---|---|
| Stephmarc, tú | Propiedades, tipos, validaciones, Flyway, disponibilidad, reservas y Feign. Frontend del catálogo y creación de reservas. | [Guía de Stephmarc](GUIA_ESTUDIO_STEPHMARC.md) |
| Alanguia | Perfil, DNI/RUC, JWT, Keycloak, PKCE, eventos, outbox y pagos. Frontend del perfil, login y consulta de pagos. | [Guía de Alanguia](GUIA_ESTUDIO_ALANGUIA.md) |
| Ambos | Config, Eureka, Gateway, observabilidad, estados, autorización por dueño, pruebas y reconstrucción de Docker. | [Guía general](GUIA_ESTUDIO.md) y este ensayo |

El reparto indica qué prepara y explica cada uno. El código inicial se publicó completo; no asigna una autoría ficticia de esos commits. Para los siguientes cambios cada integrante realiza, entiende, prueba y registra su propia mejora.

## Cómo localizar una regla en menos de un minuto

En VS Code, Ctrl+P abre un archivo por nombre. Ctrl+F busca un método en ese archivo; Ctrl+Shift+F busca un texto en todo el proyecto. Los enlaces de las guías individuales permiten abrir directamente las clases y las líneas relevantes en GitHub.

| Lo que pide el docente | Dónde empezar | Qué más debes revisar |
|---|---|---|
| Cambiar un título o texto | Plantilla del componente Angular | Que no hayas cambiado el valor interno de un estado |
| Cambiar una regla de entrada | DTO Request y `@Valid` en Controller | Formulario y prueba negativa por API |
| Cambiar una regla del negocio | Service del dueño del dato | Estado persistido, consumidores y pruebas |
| Cambiar un campo guardado | Entity y nueva migración Flyway | Request, response, mapper, actualización manual, pantalla y scripts |
| Cambiar quién puede acceder | SecurityConfig y autorización del servicio | Guard de Angular solo como experiencia de uso; prueba con dos cuentas |
| Cambiar el recorrido de una llamada | Cliente Feign, ruta Gateway o configuración | Nombres Eureka, emisor JWT y URL del frontend |
| Cambiar un evento | Productor y consumidor, con sus clases de evento | Topic, tipo de evento, clave, duplicados y compatibilidad |
| Mostrar por qué falló | Logs y `X-Trace-ID`, Grafana, métricas | Respuesta HTTP, estado final y dependencia que no respondió |

**Regla para la exposición:** primero expliquen qué capas toca el cambio; después editen. Así pueden distinguir un cambio de texto de uno que necesita migración, contrato y varias aplicaciones.

## Guion compartido de unos doce minutos

| Tiempo orientativo | Quién | Código o demostración |
|---|---|---|
| 1 minuto | Ambos | Arquitectura y recorrido propiedad → reserva → pago → resultado. Cada servicio tiene su base lógica. |
| 2 minutos | Tú | PropiedadController, DTO y Service. Crear y actualizar una propiedad como ADMIN. |
| 2 minutos | Tú | ReservaController y `ReservaServiceImpl.crear()`: dueño JWT, Feign, precio del servidor y disponibilidad. |
| 2 minutos | Alanguia | Perfil y login: `ClienteController`, `auth-service.ts`, PKCE y roles. |
| 2 minutos | Alanguia | Consumidor, `PagoServiceImpl.procesar()`, outbox y evento de resultado. Mostrar aprobado y rechazado. |
| 1 minuto | Ambos | Compensación y polling. Pagar cambia a PAGADA; rechazar libera disponibilidad y cancela. |
| 2 minutos | Ambos | Dos usuarios, 404 de objeto ajeno, dos instancias y una traza en logs. |

Frase de enlace entre intervenciones: «La reserva ya está guardada y pendiente; ahora el otro servicio procesará su evento». Al volver: «El resultado llegó por Kafka; reserva comprueba su estado y aplica la transición».

No lean todas las clases en pantalla. Muestren una entrada, una regla y una salida en cada bloque. El docente puede pedir cualquier archivo: ambos deben reconocer las piezas compartidas.

## Preparar el entorno antes de la clase

Trabajen sobre el repositorio clonado, no sobre el ZIP. Comprueben primero si hay cambios locales:

```powershell
git status

# Solo con el trabajo local guardado y una rama apropiada
git pull --ff-only

# Ejemplo: una rama nueva para cada ensayo
git switch -c ensayo/stephmarc-validacion
# En la computadora de Alanguia puede usar ensayo/alanguia-pagos
```

Desde la raíz del proyecto pueden iniciar todo con:

```powershell
.\scripts\iniciar.ps1
docker compose -f compose.json ps
```

El script usa prod local por defecto. `dev` se selecciona con `-Perfil dev` y habilita más diagnóstico, incluido Swagger de propiedad. Usen un ambiente coherente durante la demostración.

Confirmen el frontend en `http://localhost:4200`, Gateway en `http://localhost:18080`, Eureka en `http://localhost:18761` y Grafana en `http://localhost:13000`. Las cuentas de aplicación de demostración son `cliente1`, `cliente2` y `admin`, con contraseña `Casa2026!`. No son las credenciales de la consola administrativa de Keycloak.

Dejen preparados dos inmuebles disponibles distintos para aprobación y rechazo. Si usan uno ya reservado, estarán demostrando falta de disponibilidad en lugar de la regla nueva. Los datos son de prueba y el pago es simulado.

## Secuencia para resolver un cambio en vivo

1. Reformulen la petición: «¿El mínimo es inclusivo? ¿Es obligatorio o puede estar vacío? ¿Es una etiqueta o un estado?». Eso evita implementar una regla distinta.
2. Localicen el dueño de la regla y los archivos relacionados con la tabla anterior.
3. Muestren el comportamiento anterior con un dato concreto, si sigue siendo posible en la demostración.
4. Hagan el cambio más pequeño que satisfaga la petición. No cambien roles, topics, estados y esquema al mismo tiempo sin necesidad.
5. Compilen y ejecuten las pruebas pertinentes. Si modificaron una regla, añadan o adapten una prueba que la distinga del comportamiento anterior.
6. Actualicen los contenedores de las aplicaciones modificadas. Compilar Java no actualiza una imagen Docker que ya estaba ejecutándose.
7. Repitan el mismo caso y un caso válido de control. Muestren la respuesta HTTP o el estado persistido, además del mensaje visual.
8. Revisen `git diff`, guarden el cambio con un mensaje concreto y expliquen qué comprobaron.

## Cambios en Angular

Desde `clients/casa-frontend`, con una versión compatible de Node, se puede comprobar la interfaz localmente:

```powershell
npm ci
npm test
npm run build
```

`npm ci` es necesario al preparar dependencias o cuando cambia el lockfile; no hace falta repetirlo en cada modificación de texto. `npm test` ejecuta las pruebas actuales de autenticación del frontend, no una prueba automática de todas las pantallas. La compilación comprueba TypeScript y las plantillas; los cambios visuales se revisan también en el navegador.

Para actualizar la interfaz servida por Docker, desde la raíz del repositorio:

```powershell
$env:CASA_PROFILE = 'prod'
docker compose -f compose.json build frontend
docker compose -f compose.json up -d --no-deps frontend
```

Después recarguen la página. Si se perdió la sesión al recargar, completen el login de nuevo antes de atribuir un 401 a la lógica que cambiaron.

## Cambios en Java

Ejemplo de un cambio en pago, desde la raíz:

```powershell
mvn -f services/casa-pago-ms/pom.xml package
$env:CASA_PROFILE = 'prod'
docker compose -f compose.json build pago
docker compose -f compose.json up -d --no-deps pago
docker compose -f compose.json logs --tail 30 pago
```

`package` compila y ejecuta las pruebas existentes del módulo. El módulo de cliente no tiene actualmente una suite propia equivalente a las de propiedad, reserva y pago: allí deben probar la API de perfil directamente y ampliar pruebas cuando añadan reglas.

Correspondencia entre módulo y servicio del compose:

| Módulo Java | Servicio que reconstruyes |
|---|---|
| `services/casa-propiedad-ms` | `propiedad` y `propiedad2` |
| `services/casa-reserva-ms` | `reserva` |
| `services/casa-pago-ms` | `pago` |
| `services/casa-cliente-ms` | `cliente` |
| `infra/casa-gateway` | `gateway` |

Usen `--no-deps` para actualizar solo los servicios ya preparados que modificaron. Si cambian un contrato compartido o configuración, revisen qué dependencias también necesitan reiniciarse. No den por listo un servicio solo porque el contenedor diga `Started`: esperen su disponibilidad y comprueben los logs si la API no responde.

## Cambios en la base de datos

Una columna nueva requiere una migración nueva. No editen las migraciones que Flyway ya aplicó. Antes de elegir el número, miren los archivos existentes: propiedad llega a V4 y cliente tiene V1 en esta entrega, pero eso puede cambiar con sus próximos avances.

Primero decidan cómo conservar datos anteriores: nullable o valor por defecto coherente. Después revisen la entidad, DTO, copia en actualización, mapper, formulario y scripts de prueba. Cambiar solo la entidad puede impedir el arranque porque Hibernate valida el esquema.

Si aparece un error de checksum, revisen qué migración antigua se alteró y recuperen su versión correcta desde Git. No borren la base ni cambien el historial de Flyway para ocultar el problema durante la exposición.

## Cómo probar una regla sin confundir los resultados

Para una validación nueva usen dos requests completos e iguales salvo el campo de prueba. Un 400 con JSON incompleto no demuestra que el mínimo del precio o el patrón del teléfono funcionen.

Para autorización usen el mismo ID con dos cuentas distintas. Para un pago rechazado usen una propiedad disponible y esperen la transición asíncrona. Para un nuevo campo revisen crear, consultar y actualizar: es frecuente que la creación funcione y la actualización manual no copie el dato.

Las funciones `login()` y `call()` de [`scripts/verificar.py`](../scripts/verificar.py) muestran cómo autenticar solicitudes de prueba sin depender de Postman. Pueden reutilizarlas desde una consola Python iniciada en la raíz:

```python
import sys
sys.path.insert(0, 'scripts')
import verificar as v

token = v.login('admin')
status, respuesta, cabeceras = v.call('/api/v1/propiedades', token=token)
print(status, respuesta)
```

Para POST o PUT, esa función recibe método, un diccionario con todos los campos válidos y token. No impriman ni compartan los JWT en capturas. Los scripts de integración crean registros de prueba: la evidencia nueva puede añadir datos al catálogo.

## Simulacro con cambios repartidos

| Ronda | Tú | Alanguia | Resultado que deben mostrar |
|---|---|---|---|
| 1 | Añadir dirección a la búsqueda | Cambiar texto de guardar perfil | Pantalla actualizada; las llamadas API siguen funcionando |
| 2 | Mínimo de precio de reserva | Patrón de nueve dígitos para teléfono | Request válido aceptado e inválido rechazado por backend |
| 3 | Explicar bloqueo y dueño de reserva | Añadir otro método de pago rechazado | Reserva cancelada y disponibilidad restaurada |
| 4 | Elegirle una clase de clientes o pago | Elegirte una clase de propiedad o reserva | Cada uno explica entrada, regla, persistencia y salida del código del otro |

Empiecen por las rondas 1 y 2; la tercera cruza contratos y requiere coordinación. El campo nuevo con migración es un ejercicio avanzado de las guías individuales, no el primer cambio que conviene improvisar.

## Qué deben poder explicar ambos

- Controller recibe HTTP; DTO valida entrada; Service aplica negocio; Repository persiste. Cliente es una simplificación que actualmente conecta Controller y Repository directamente.
- El dueño de una reserva o perfil viene del JWT validado, no de un ID que el usuario manda libremente.
- Un rol decide permisos generales; el dueño del registro se comprueba en el servicio que lo administra.
- Outbox hace reintentable la publicación local; el consumidor todavía necesita idempotencia.
- La SAGA de rechazo compensa disponibilidad; no es un rollback global entre bases.
- El cupo representa disponibilidad y el precio es de separación. Fotos y pagos son parte de una demostración académica.
- El polling consulta resultados posteriores; no confirma el pago por sí mismo.
- Construir, ejecutar y probar son pasos distintos. Las verificaciones históricas están en `evidencias`, pero sus nuevos cambios necesitan la comprobación correspondiente.

Al terminar el ensayo, cada uno debe poder hacer una modificación de interfaz y una validación pequeña, reconstruir la aplicación afectada y explicar por qué el caso negativo falla y el positivo sigue funcionando.
