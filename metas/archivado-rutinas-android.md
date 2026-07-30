# Archivado de rutinas Android

## Objetivo

Permitir que una persona autenticada archive una rutina que ya no utiliza para retirarla de sus rutinas activas sin borrar las sesiones ni el progreso historico asociados.

## Restricciones

- El archivado es reversible mediante estado; no se elimina fisicamente workout_plans ni sus sesiones.
- Solo el propietario autenticado puede archivar o restaurar una rutina.
- Mantener fuera de alcance eliminacion definitiva, duplicacion, plantillas y recomendaciones automaticas.

## Listo cuando

- [x] El backend almacena el estado activo o archivado, excluye las archivadas de la lista activa y conserva las sesiones historicas.
- [x] El backend rechaza archivar o restaurar una rutina ajena y responde de forma coherente ante una rutina inexistente.
- [x] El backend expone GET /workout-plans/archived y solo devuelve las rutinas archivadas del propietario autenticado.
- [x] Android permite archivar una rutina desde Mis rutinas tras una confirmacion y la retira de la lista activa al completarse.
- [x] Android permite abrir Archivadas y restaurar una rutina, devolviendola a la lista activa sin crear una segunda rutina.
- [x] Los estados de carga y error Android son recuperables y conservan la lista visible mientras se reintenta.
- [x] Las pruebas Android y backend cubren las acciones, confirmacion, restauracion y error; las suites completas de Android y backend finalizan correctamente.
- [x] En el AVD se archiva una rutina existente, se comprueba que su sesion sigue en Historial, se restaura y vuelve a aparecer como activa.
