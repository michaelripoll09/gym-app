# Catálogo inicial de ejercicios

## Fuente aprobada

La fuente inicial es `hasaneyldrm/exercises-dataset`, versión fijada por commit antes de cada importación. Gym App no consulta ese repositorio en tiempo de ejecución: el archivo `data/exercises.json` se descarga durante la preparación del catálogo, se valida y se importa a PostgreSQL.

## Datos incorporados

- Identificador externo de la fuente, nombre, categoría, parte corporal, equipo, músculo objetivo y músculos secundarios.
- Instrucciones y pasos en español; inglés se conserva como respaldo editorial.
- Fecha de la fuente, commit de origen y hash SHA-256 del archivo importado para trazabilidad.

## Datos excluidos

Las miniaturas y GIFs no se copian ni se muestran en el lanzamiento. El repositorio indica que esos medios pertenecen a Gym Visual y requieren licencia independiente. El importador preserva la atribución recibida, pero no descarga archivos de medios.

## Curaduría de Gym App

Los datos de origen describen músculos y equipo, no disciplinas. Un archivo editorial versionado asigna a cada ejercicio publicado cero o más perfiles: fitness general, bodybuilding, powerlifting, running, CrossFit y calistenia. La publicación requiere revisión humana; un ejercicio sin mapeo no aparece en recomendaciones ni en el catálogo de la app.

El lanzamiento usa un subconjunto curado. El resto queda importado como no publicado para futuras revisiones. La curaduría también puede excluir ejercicios que requieran equipo poco común, tengan instrucciones insuficientes o no sean apropiados para el nivel del usuario.

## Proceso reproducible

1. Fijar el commit fuente y descargar únicamente `data/exercises.json` y su JSON Schema.
2. Validar el archivo contra el esquema y comprobar su hash.
3. Transformar las columnas de origen al modelo interno sin alterar el identificador externo.
4. Aplicar el mapeo editorial de perfiles y estado de publicación.
5. Rechazar registros duplicados, sin instrucciones en español o sin mapeo publicado.
6. Generar un reporte: importados, publicados, excluidos y motivos.
