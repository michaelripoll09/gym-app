# Registro de sesión Android

## Objetivo

Permitir que una persona con una rutina guardada inicie esa rutina desde Android, registre las repeticiones realizadas en cada serie y guarde una sesión de entrenamiento autenticada, para completar el ciclo principal de crear, ejecutar y registrar entrenamiento.

## Restricciones

- Reutilizar los planes persistidos, el endpoint de sesiones existente, el token de sesión y la paleta oscura con acento verde lima.
- Mantener fuera de alcance cronómetro, seguimiento de peso/carga, métricas históricas, IA, pagos Premium, gimnasios y funciones sociales.

## Listo cuando

- [ ] Cada rutina mostrada en “Mis rutinas” ofrece una acción visible para iniciarla.
- [ ] La pantalla de sesión muestra los ejercicios y las series planificadas de la rutina seleccionada.
- [ ] La persona puede introducir las repeticiones realizadas para cada serie y la app rechaza valores vacíos o no positivos antes del envío.
- [ ] Al finalizar una sesión válida, Android envía el registro autenticado a `POST /api/v1/workout-plans/{planId}/sessions` y muestra confirmación o un error recuperable.
- [ ] Una sesión guardada devuelve a “Mis rutinas” sin perder la rutina utilizada y no permite reenviar accidentalmente mientras la solicitud está en curso.
- [ ] Las pruebas de backend cubren la creación y propiedad de la sesión, las pruebas Android cubren la validación del registro y ambas suites completas finalizan correctamente.
