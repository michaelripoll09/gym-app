# Rutinas Android

## Objetivo

Completar y validar de extremo a extremo la creación de rutinas manuales en Android para la persona autenticada: partir del catálogo, configurar una rutina y volver a consultarla persistida tras reiniciar la aplicación.

## Restricciones

- Usar la API de planes de entrenamiento, el token de sesión actual y la paleta oscura con acento verde lima.
- Reiniciar el backend con el código y las migraciones actuales antes de ejecutar la validación Android.
- Mantener fuera de alcance IA, pagos Premium, gestión de gimnasios y funciones sociales.

## Listo cuando

- [ ] El backend activo incluye la migración de descanso y responde autenticadamente a `GET /api/v1/workout-plans`.
- [ ] Desde el catálogo de ejercicios autenticado existe una acción visible para iniciar la creación de una rutina.
- [ ] La pantalla de creación permite definir nombre, uno o más días programados y uno o más ejercicios del catálogo.
- [ ] Cada ejercicio añadido permite definir y validar series, repeticiones y tiempo de descanso mayores que cero antes del envío.
- [ ] Al guardar una rutina válida desde Android, la API autenticada responde exitosamente y la app muestra la sección de rutinas sin error.
- [ ] Tras cerrar y volver a abrir la aplicación con la misma cuenta, la rutina creada se consulta desde “Mis rutinas” con sus días, ejercicios y descanso persistidos.
- [ ] Las pruebas completas de backend y Android finalizan correctamente.
