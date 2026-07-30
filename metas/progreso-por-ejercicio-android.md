# Progreso por ejercicio Android

## Objetivo

Mostrar a las personas autenticadas un resumen de progreso por ejercicio a partir de sus sesiones guardadas, indicando la última carga registrada y la carga máxima para que puedan identificar avances de fuerza sin requerir una herramienta externa.

## Restricciones

- Reutilizar GET /workout-sessions y las cargas opcionales ya guardadas; no crear endpoints, tablas ni gráficas nuevas.
- Considerar únicamente series que tengan carga en kg y excluir de forma explícita las series sin carga para no inventar progreso en ejercicios de peso corporal.
- La prueba RED ya está escrita. No existe un bloqueo externo: se debe crear el modelo `ExerciseLoadProgress`, implementar `exerciseLoadProgress(sessions)` ordenando por sesión con carga más reciente y nombre, y mostrar el resultado en la pantalla Progreso.

## Listo cuando

- [ ] La pantalla Progreso muestra una sección Por ejercicio con los ejercicios que tienen al menos una carga registrada.
- [ ] Cada ejercicio muestra la última carga registrada y la mayor carga registrada, calculadas a partir de las sesiones del usuario.
- [ ] Las series sin carga no aparecen como 0 kg ni afectan los cálculos; si no hay cargas, se muestra un estado vacío explicativo.
- [ ] Los ejercicios se ordenan por la sesión con carga más reciente y los empates se resuelven por nombre.
- [ ] Los estados de carga, error y reintento existentes se conservan al consultar el historial de sesiones.
- [ ] Las pruebas Android cubren máximo, última carga, series sin carga, vacío y ordenamiento; la suite Android finaliza correctamente.
- [ ] El emulador verifica que una sesión con carga se refleje en Progreso por ejercicio.
