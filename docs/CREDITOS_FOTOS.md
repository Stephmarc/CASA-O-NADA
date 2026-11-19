# Fotografías de referencia

Estas fotografías reales ilustran las categorías del prototipo. No representan las propiedades ficticias, sus direcciones ni ofertas inmobiliarias reales. Se repite una fotografía por tipo: casa, departamento o terreno. Cada tarjeta lo indica y enlaza a su autor.

Se distribuyen archivos locales optimizados para evitar depender de servidores de imágenes al abrir el catálogo.

| Archivo en `clients/casa-frontend/public/images/` | Autor | Fuente |
|---|---|---|
| `casa-referencia.jpg` | jirayu koontholjinda | [Casa con jardín, Bangkok](https://unsplash.com/photos/UnUFaL8alf4) |
| `departamento-referencia.jpg` | Yansi Keim | [Edificio de departamentos](https://unsplash.com/photos/_4U19TDp1bw) |
| `terreno-referencia.jpg` | Jim McCormick | [Campo verde](https://unsplash.com/photos/brZ9zzbcqKw) |

Licencia consultada: [Unsplash License](https://unsplash.com/license). Los archivos se descargaron como JPEG de 960 × 640 píxeles para la interfaz. No se utilizó generación de imágenes.

Para reemplazarlas por fotografías propias de cada inmueble, el siguiente avance sería guardar una URL de imagen en `Propiedad`, exponerla en su DTO y usarla en `FotoPropiedad`. Actualmente la selección es ilustrativa por categoría, sin cambios en la base de datos.
