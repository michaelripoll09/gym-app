# Hitos de progreso Android

## Objetivo

Comunicar a cada persona cuando una sesion completada establece un nuevo record personal de carga o repeticiones, para reforzar un progreso verificable sin apartarla de su flujo de entrenamiento.

## Restricciones

- El backend determina los hitos solo con sesiones sincronizadas del usuario autenticado y compara contra los registros previos a la sesion creada.
- Un empate con una marca existente no genera un hito; solo una mejora estricta de carga o repeticiones puede hacerlo.
- Los hitos identifican ejercicio, tipo de record, valor y fecha, pero no incluyen clasificaciones, comparaciones sociales, funciones premium ni recomendaciones medicas.
- Android muestra los hitos inmediatamente despues de guardar una sesion y permite continuar sin requerir una accion adicional.
- Corregir, eliminar o sincronizar sesiones no debe dejar records ni mensajes persistentes desactualizados; la fuente de verdad continua siendo el historial del backend.

## Listo cuando

- [ ] El registro de una sesion devuelve de forma autenticada los hitos nuevos de carga o repeticiones que genero esa sesion, sin revelar datos de otros usuarios.
- [ ] El calculo ignora empates, usa solo registros anteriores como referencia y devuelve el ejercicio, tipo, valor y fecha de cada mejora estricta.
- [ ] Android presenta un resumen claro de los hitos recibidos despues de finalizar una sesion y mantiene el flujo normal cuando no se genero ninguno.
- [ ] Los refrescos posteriores a correcciones, eliminaciones y sincronizaciones muestran records consistentes y no conservan hitos obsoletos localmente.
- [ ] Las pruebas de backend y Android cubren propiedad, mejoras, empates, ausencia de hitos, interfaz y refresco, y las suites correspondientes finalizan en PASS.
