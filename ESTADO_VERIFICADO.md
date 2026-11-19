# Estado verificado de CASA O NADA

Verificación realizada el 19 de noviembre de 2026, zona America/Lima.

| Verificación ejecutada | Resultado |
|---|---|
| Compilación de ocho aplicaciones Java y reactor raíz | Correcta |
| Pruebas de comportamiento Java | 11 aprobadas, 0 fallos |
| Compilación Angular 22 | Correcta |
| Pruebas PKCE y manejo de JWT del cliente | 3 aprobadas |
| Integración por Gateway en dev | 25 aprobadas |
| Integración por Gateway en prod local | 25 aprobadas |
| Kafka apagado, recuperación, duplicados y caída de propiedad | 4 comprobaciones aprobadas |
| Balanceo por Gateway | Ambas instancias de propiedad reciben solicitudes |
| Prometheus | 9 targets UP |
| Config Server | Perfiles dev/prod consultados y diferenciados |
| JWT propio S07 | Registro, login, claims RS256/CLIENTE y JWKS comprobados |
| Navegador | Login PKCE real, perfil cargado y reservas con pago aprobado/rechazado visibles |

Los resultados están en `evidencias/`. `aplicacion.jpg` es una captura de verificación del producto; no sustituye las capturas individuales con reloj y perfil que solicitan las rúbricas. Las pruebas usan cuentas ficticias y pagos simulados.

## Entrega

El ZIP contiene fuentes completas, migraciones, Dockerfiles, compose integrado, frontend compilado, scripts, documentación y resultados. No incluye cachés de dependencias ni JAR compilados: `scripts/iniciar.ps1` los reconstruye, ejecuta las pruebas y arranca Docker.

Para abrir el proyecto revisa `README.md`. El frontend queda en http://localhost:4200 y el usuario de demostración es `cliente1` con contraseña `Casa2026!`. El administrador de la aplicación es `admin` con la misma contraseña. Son datos locales de demostración.

## Lo que depende de ustedes

- Para consultar datos reales DNI/RUC, configurar una credencial válida del proveedor en `DOCUMENTOS_TOKEN`. Sin ella la consulta externa responde 503 y el perfil puede registrarse manualmente. No se verificó la consulta a datos reales.
- Completar nombres, equipo, repositorio, capturas personales, reflexiones y defensas de las evaluaciones S05/S12/S15/S16 según las guías. El guion y la propuesta de reparto están preparados; no se inventaron contribuciones ni aprobaciones académicas.
- El sistema es una implementación académica en producción local. Los pagos son una simulación y no procesa transacciones inmobiliarias reales.

Base reconocida del docente: https://github.com/262dist/pagatu, commit `ccc2e39da3cde7076fda31e3acbbb7c985a6c769`. Autor de las guías: Angel Sullon Macalupu.
