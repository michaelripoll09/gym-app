# Historial de sesiones Android

## Objetivo

Completar para la persona autenticada un historial de entrenamientos en Android: acceder desde el flujo principal, consultar sus sesiones ordenadas por fecha y abrir el detalle de rutina, ejercicios, series y repeticiones registradas; además, validar el recorrido con datos reales en el emulador.

## Restricciones

- Reutilizar los planes y sesiones existentes, proteger estrictamente cada historial por propietario y mantener la paleta oscura con acento verde lima.
- Mantener fuera de alcance gráficos, estadísticas agregadas, comparaciones entre periodos, IA, pagos Premium, gimnasios y funciones sociales.

## Listo cuando

- [ ] La API autenticada expone sesiones exclusivamente del usuario propietario, ordenadas de la más reciente a la más antigua, con rutina, fecha, ejercicios, series y repeticiones registradas.
- [ ] Android ofrece una acción visible de “Historial” dentro del flujo principal de entrenamiento y permite volver sin perder una navegación funcional.
- [ ] La pantalla de historial carga y presenta las sesiones existentes del usuario con un estado vacío claro cuando aún no ha completado ninguna.
- [ ] Al seleccionar una sesión, Android muestra su detalle con nombre de rutina, fecha de inicio y las series y repeticiones de cada ejercicio.
- [ ] Si la carga falla, Android comunica un error recuperable y ofrece reintentar sin bloquear la navegación.
- [ ] Las pruebas de backend verifican creación, orden y aislamiento por propietario; las pruebas Android cubren carga, vacío, error y detalle, y ambas suites completas finalizan correctamente.
- [ ] En el emulador se verifica el recorrido completo usando una sesión real ya registrada: abrir Historial, ver la sesión y abrir su detalle.
