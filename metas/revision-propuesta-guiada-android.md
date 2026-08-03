# Revisión de propuesta guiada Android

## Objetivo

Permitir que la persona que recibe una rutina guiada revise y ajuste su propuesta antes de crearla, para que pueda adaptar días, ejercicios, series, repeticiones y descansos a su realidad sin modificar ninguna rutina ya guardada.

## Restricciones

- La propuesta sigue siendo un borrador local hasta que la persona pulsa crear; descartar o volver no debe persistir cambios.
- Solo se pueden mantener o seleccionar ejercicios publicados compatibles con el perfil principal de la persona.
- Conservar el generador determinista actual como fuente de la propuesta; excluir chat conversacional, IA externa, pagos, gimnasios y recomendaciones médicas.

## Listo cuando

- [ ] Android permite editar en la propuesta guiada el nombre, los días, series, rango de repeticiones, descanso y los ejercicios incluidos antes de confirmarla.
- [ ] Android permite quitar un ejercicio, sustituirlo por uno compatible del catálogo o descartar todo el borrador sin crear ni alterar rutinas existentes.
- [ ] La confirmación crea exactamente la versión editada del borrador y el backend rechaza cualquier ejercicio no publicado o incompatible con el perfil.
- [ ] La pantalla explica claramente que se trata de una propuesta editable y conserva estados de carga, error recuperable y confirmación en curso.
- [ ] Pruebas Android y backend cubren edición válida, sustitución compatible, descarte, confirmación de los cambios y rechazo de una sustitución incompatible; el emulador valida editar y crear el resultado.
