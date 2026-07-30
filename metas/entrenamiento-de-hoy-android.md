# Entrenamiento de hoy Android

## Objetivo

Mostrar a la persona autenticada una entrada diaria en Android con las rutinas activas programadas para el día local actual, para que pueda iniciar su entrenamiento sin buscarlo manualmente en el catálogo o la lista de rutinas.

## Restricciones

- Reutilizar GET /workout-plans y los días ya guardados en cada rutina; no crear nuevas tablas ni endpoints de planificación.
- Usar la zona horaria local del dispositivo para resolver el día actual y mantener la estética oscura con acento verde lima.
- Mantener fuera de alcance notificaciones, recordatorios, recomendaciones automáticas, calendario externo y reprogramación por fecha.
- No existe un bloqueo actual. Si una carga falla, la pantalla debe conservar el contenido previamente cargado cuando exista y ofrecer Reintentar; si el fallo persiste, se debe comprobar la sesión autenticada y la disponibilidad de GET /workout-plans antes de cambiar la interfaz.

## Listo cuando

- [ ] La lógica `plansForToday` selecciona únicamente planes activos cuyo día coincide con el día local, cubre los siete días y descarta días no reconocidos y listas vacías.
- [ ] Android muestra una entrada Entrenamiento de hoy desde la navegación principal, consulta GET /workout-plans y presenta solo las rutinas activas obtenidas para hoy.
- [ ] Cada rutina de hoy muestra nombre, ejercicios y un acceso directo que inicia la sesión existente para esa rutina.
- [ ] Si no hay rutina para hoy, se muestra un estado vacío con acceso a Mis rutinas sin crear ni modificar planes.
- [ ] Los estados de carga y error son recuperables mediante Reintentar y conservan la información previa cuando existe; un fallo persistente informa la acción de comprobar sesión y disponibilidad del servicio.
- [ ] Las pruebas Android cubren selección por día, vacío, error y acción de inicio; la suite completa de Android finaliza correctamente y el flujo se comprueba en el emulador.
