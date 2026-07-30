# Resumen semanal y adherencia Android

## Objetivo

Dar a los usuarios autenticados un resumen semanal útil de su entrenamiento completado, comparándolo con la programación activa y mostrando su adherencia, volumen y continuidad para que puedan decidir qué sesión realizar después.

## Restricciones

- Reutilizar las sesiones, rutinas y ejercicios ya registrados; no crear una segunda fuente de historial.
- El resumen debe respetar la semana local del usuario y no modificar sesiones ni rutinas.
- Incluir API, Android y pruebas automatizadas; conservar el diseño visual actual de la aplicación.

## Listo cuando

- [ ] La API devuelve para la semana actual las sesiones completadas, las sesiones programadas y el porcentaje de adherencia del usuario autenticado.
- [ ] La API calcula y devuelve el volumen semanal a partir de las series completadas, sin contar sesiones incompletas o archivadas.
- [ ] Android muestra un acceso al resumen semanal desde el flujo principal y estados de carga, vacío, error con reintento y contenido.
- [ ] La pantalla muestra sesiones realizadas frente a programadas, adherencia, volumen y una indicación clara de la próxima sesión disponible.
- [ ] El resumen se actualiza al volver de completar una sesión y no altera la rutina ni el historial existentes.
- [ ] Las pruebas de backend cubren semana sin datos, semana con sesiones y cálculo de adherencia y volumen; las pruebas Android cubren los estados y métricas mostradas.
- [ ] La suite de backend, la suite Android y una validación en emulador confirman el flujo completo.
