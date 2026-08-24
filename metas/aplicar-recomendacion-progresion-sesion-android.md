# Aplicar recomendación de progresión en sesión Android

## Objetivo

Permitir que la persona que está registrando una sesión aplique de forma explícita la recomendación de progresión del ejercicio actual a su borrador de series, con una confirmación clara y la posibilidad de conservar o editar manualmente cualquier valor, para reducir trabajo repetitivo sin perder control sobre el registro.

## Restricciones

- La aplicación debe requerir una acción explícita de la persona; nunca puede ocurrir al cargar la recomendación ni al abrir la sesión.
- Aplicar una recomendación solo puede modificar el borrador del ejercicio correspondiente; no modifica rutinas, referencias guardadas, historial ni otros ejercicios.
- Antes de confirmar, mostrar carga, repeticiones y la explicación recibida; permitir cancelar sin cambiar el borrador.
- Conservar la edición manual y el guardado al finalizar existentes; no aplicar recomendaciones cuando no haya una recomendación válida.
- Mantener el flujo sin conexión: la acción opera sobre el borrador local y no depende de una nueva llamada de red.

## Listo cuando

- [ ] Cada ejercicio con una recomendación válida muestra una acción explícita para revisarla y aplicarla, mientras que los ejercicios sin recomendación no muestran una acción aplicable.
- [ ] La confirmación muestra la carga, las repeticiones y la explicación de la recomendación, y cancelar deja intactos todos los campos manuales.
- [ ] Confirmar actualiza únicamente las series editables del ejercicio correspondiente en el borrador local y la pantalla refleja los valores aplicados.
- [ ] Después de aplicar, la persona puede editar manualmente cualquier carga o repetición y guardar la sesión con esos valores sin una nueva aplicación automática.
- [ ] Las pruebas Android cubren confirmación, cancelación, alcance por ejercicio, edición manual posterior y ausencia de recomendación; la suite Android finaliza en PASS.
