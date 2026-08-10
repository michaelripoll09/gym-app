# Revisión de rutina basada en progreso Android

## Objetivo

Permitir que cada persona revise sugerencias transparentes para su rutina activa basadas en su progreso reciente, para decidir si desea ajustar ejercicios, series, repeticiones o descansos sin que Gym App modifique su programación de forma automática.

## Restricciones

- Las sugerencias usan únicamente sesiones sincronizadas, adherencia, récords, progresión y perfil de entrenamiento de la cuenta autenticada; no exponen datos a gimnasios ni a otros usuarios.
- Cada sugerencia identifica la regla y los datos que la originan, se limita al catálogo compatible con el perfil y no diagnostica, prescribe tratamiento ni sustituye a un profesional de salud.
- La rutina activa nunca se altera por consultar una sugerencia; la persona debe abrir la edición y confirmar explícitamente cualquier cambio mediante el flujo existente.
- Este bloque no incorpora chat, modelos de IA externos, Premium, pagos, clasificaciones sociales ni cambios automáticos de cargas registradas.

## Listo cuando

- [ ] El backend expone sugerencias autenticadas para la rutina activa, con el ejercicio o día afectado, la acción propuesta, explicación y fuentes de progreso propias.
- [ ] Las reglas producen estados explícitos cuando no existe rutina activa o todavía no hay datos suficientes, y no devuelven datos de otra cuenta ni ejercicios incompatibles con el perfil.
- [ ] Android permite abrir la revisión desde Rutinas o Progreso, presenta carga, vacío, error recuperable y sugerencias legibles antes de cualquier edición.
- [ ] Cada sugerencia puede llevar a la edición manual de la rutina correspondiente sin aplicar cambios por sí misma; cancelar o volver conserva la rutina activa intacta.
- [ ] Corregir, eliminar o sincronizar sesiones, y activar, editar o archivar una rutina, refresca las sugerencias al volver a consultarlas.
- [ ] Las pruebas de backend y Android cubren propiedad, reglas, falta de datos, compatibilidad, navegación sin mutación y refrescos, y las suites correspondientes finalizan en PASS.
