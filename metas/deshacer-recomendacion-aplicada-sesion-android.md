# Deshacer recomendación aplicada en sesión Android

## Objetivo

Permitir que la persona que aplicó explícitamente una recomendación de progresión revierta esa única aplicación antes de guardar la sesión, restaurando los valores locales anteriores del ejercicio afectado para mantener el control y corregir una confirmación accidental.

## Restricciones

- La reversión solo está disponible después de una aplicación confirmada de recomendación y antes de que la persona edite manualmente una serie del mismo ejercicio; una edición manual invalida la reversión para no descartar cambios intencionales.
- Guardar un único estado anterior por ejercicio aplicado dentro del borrador local actual; no persistirlo, sincronizarlo ni modificar historial, referencias, rutinas u otros ejercicios.
- La acción no puede hacer llamadas de red ni crear, modificar o eliminar recomendaciones; debe funcionar sin conexión.
- Aplicar una nueva recomendación reemplaza únicamente la reversión pendiente de ese mismo ejercicio, y cancelar o guardar la sesión elimina cualquier reversión pendiente.

## Listo cuando

- [ ] Tras confirmar una recomendación válida, la pantalla muestra una acción explícita para deshacerla solo en el ejercicio correspondiente.
- [ ] Deshacer restaura exactamente las cargas y repeticiones del borrador que existían antes de aplicar la recomendación y no modifica series de otros ejercicios.
- [ ] Editar manualmente una carga o repetición del ejercicio aplicado, aplicar una nueva recomendación, cancelar la sesión o guardar la sesión elimina o reemplaza la reversión pendiente según corresponda.
- [ ] La reversión opera únicamente sobre estado local, no ejecuta llamadas de red y conserva el flujo sin conexión y el guardado existente.
- [ ] Las pruebas Android cubren disponibilidad, restauración exacta, alcance por ejercicio, invalidación por edición manual y reemplazo o limpieza de la reversión; la suite Android finaliza en PASS.
