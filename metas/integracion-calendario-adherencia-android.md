# Integración de calendario de adherencia Android

## Objetivo

Completar para las personas autenticadas la integración del calendario mensual de adherencia ya iniciado, conectándolo desde Inicio y Progreso, mostrando datos del endpoint mensual y verificando el flujo antes de abrir una nueva funcionalidad.

## Restricciones

- Reutilizar `GET /api/v1/training-calendar`, `TrainingCalendarState` y la pantalla mensual existentes; no añadir nuevas capacidades de producto ni modificar sesiones, rutinas u objetivos.
- Conservar retorno al origen, zona horaria local, estados de carga, vacío, error y reintento, además de la paleta oscura con verde lima.
- Corregir cualquier fallo de compilación o prueba solamente en el código del calendario y dejar las suites completas en verde antes de considerar el bloque terminado.

## Listo cuando

- [ ] Inicio y Progreso ofrecen un acceso visible al calendario y el regreso devuelve a la pantalla desde la que se abrió.
- [ ] El cambio de mes solicita y muestra los datos del endpoint para el mes y la zona local seleccionados.
- [ ] El calendario muestra los estados completado, programado y sin entrenamiento, y una pulsación sobre un día completado presenta su resumen de sesión.
- [ ] Un fallo de actualización conserva los días descargados y ofrece reintento sin bloquear la navegación.
- [ ] Las pruebas dirigidas de backend y Android, y las suites completas de ambos proyectos, terminan correctamente.
- [ ] El emulador valida abrir el calendario desde Inicio, navegar de mes y volver sin perder la sesión.
