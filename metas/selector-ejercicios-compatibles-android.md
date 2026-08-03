# Selector de ejercicios compatibles Android

## Objetivo

Permitir que las personas que crean o ajustan una rutina guiada encuentren, busquen y seleccionen ejercicios compatibles con su perfil para sustituir o añadir opciones en el borrador antes de crear la rutina.

## Restricciones

- El selector usa solo los ejercicios publicados que recibe el catálogo filtrado por el perfil activo.
- La búsqueda es local por nombre; no incorpora IA, servicios externos, pagos, gimnasios ni recomendaciones médicas.
- Elegir o cancelar solo modifica el borrador local hasta que la persona crea la rutina.
- Bloqueo de validación actual: la APK y las pruebas unitarias pasan, pero la automatización por coordenadas volvió al catálogo antes de observar el selector. La solución es validar de forma controlada: esperar e inspeccionar la pantalla o jerarquía de UI después de cada acción, sin encadenar toques automáticos.

## Listo cuando

- [x] El selector filtra localmente por nombre y muestra un estado vacío sin coincidencias; las pruebas unitarias Android lo verifican.
- [x] El código permite abrir un selector de pantalla completa, cancelar sin cambiar el borrador y añadir o sustituir un ejercicio compatible.
- [x] En el emulador, abrir Generar rutina guiada y esperar la pantalla Rutina guiada antes de tocar Añadir o Sustituir ejercicio.
- [x] En el emulador, confirmar que aparece Buscar ejercicio, buscar una coincidencia, seleccionarla, crear la rutina y verificar el cambio en Mis rutinas.
- [x] Repetir la validación controlada con Cancelar y comprobar que el borrador no se modifica.
