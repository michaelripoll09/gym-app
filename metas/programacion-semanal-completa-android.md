# Programación semanal completa Android

## Objetivo

Permitir que las personas autenticadas programen una rutina para cualquiera de los siete días de la semana, incluido domingo, para que Entrenamiento de hoy pueda servir por igual a quienes entrenan durante el fin de semana.

## Restricciones

- Reutilizar el modelo y los endpoints actuales de rutinas; no crear tablas ni modificar el contrato de backend.
- Mantener los nombres de día en español que usa la selección diaria y conservar la edición de rutinas existentes.
- El selector adaptable y la lista única `routineEditorDays` ya están implementados y la suite Android pasó. No hay un bloqueo técnico: el paso pendiente es instalar el APK actualizado, abrir Crear o Editar rutina y comprobar visualmente que domingo aparece en la segunda fila, se puede seleccionar y se conserva al guardar.

## Listo cuando

- [ ] El editor de rutinas muestra los siete días de lunes a domingo y permite seleccionar y deseleccionar domingo.
- [ ] Los siete controles de día se acomodan dentro de una pantalla móvil sin quedar cortados ni requerir desplazamiento horizontal.
- [ ] Guardar una rutina con domingo genera el día `Domingo` y editarla conserva esa selección.
- [ ] La validación sigue exigiendo al menos un día y no altera las rutinas existentes que solo usan lunes a sábado.
- [ ] Las pruebas Android cubren activar/desactivar domingo, conservarlo al editar y la validación de días.
- [ ] La suite Android y el emulador verifican crear o editar una rutina de domingo y verla en Entrenamiento de hoy cuando corresponda al día local.
