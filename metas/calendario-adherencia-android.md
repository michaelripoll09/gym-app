# Calendario de adherencia Android

## Objetivo

Permitir que una persona autenticada consulte un calendario mensual de entrenamiento que distinga sus días completados y los días programados de su rutina activa, para planificar con claridad y comprender su constancia más allá del resumen semanal.

## Restricciones

- Reutilizar sesiones, rutina activa y días programados existentes; cualquier agregado de API debe limitarse a una consulta autenticada de calendario y no modificar sesiones, rutinas ni objetivos.
- El calendario usa la zona horaria local de la persona y solo muestra información de su cuenta; los días futuros se muestran como programados, nunca como faltas.
- Mantener el diseño oscuro con acento verde lima, estados de carga, vacío, error recuperable y navegación de regreso a progreso o al panel de inicio.
- Quedan fuera de alcance retos, rachas, rankings, Premium, integración con calendarios externos, funciones sociales y notificaciones nuevas.
- Avance actual: Android ya cuenta con el contrato `CalendarDayResponse`, la operación `GymApi.trainingCalendar(...)`, el estado puro para cambiar de mes o conservar días descargados y su prueba inicial. Backend ya incluye `GET /api/v1/training-calendar` y una prueba de integración de propiedad, sesiones históricas y rutina activa; la pantalla mensual y sus accesos aún no están implementados.
- Bloqueo actual: la prueba de integración del endpoint devuelve un error HTTP en lugar de la lista mensual esperada. Solución: ejecutar de nuevo la prueba ya ajustada para mostrar el cuerpo de la respuesta, identificar la excepción concreta del controlador (consulta JDBC, conversión de fecha o zona), corregir solo esa causa y repetir la prueba dirigida antes de construir la interfaz y correr las suites completas.

## Listo cuando

- [ ] La API devuelve por mes los días completados y los días programados de la rutina activa, sin exponer sesiones ni planes de otra persona.
- [ ] La API no marca como pendiente o incumplido ningún día futuro y conserva los días completados aunque la rutina activa cambie después.
- [ ] Android permite abrir el calendario desde progreso o el panel de inicio y navegar al mes anterior o siguiente.
- [ ] Cada día del calendario identifica de forma accesible si fue completado, está programado o no tiene entrenamiento; al tocar un día completado muestra el resumen de su sesión.
- [ ] La pantalla presenta estados de carga, vacío, error y reintento, y mantiene datos de calendario ya descargados cuando falla una actualización recuperable.
- [ ] Las pruebas de backend cubren autorización, días futuros, rutina activa y sesiones históricas; las pruebas Android cubren la interpretación de estados y la navegación mensual.
