# Panel de inicio Android

## Objetivo

Dar a las personas autenticadas una pantalla de inicio útil al abrir Gym App, que reúna la rutina activa, el entrenamiento correspondiente al día, el avance semanal y accesos a las funciones principales para orientar el siguiente entrenamiento sin buscar entre pantallas.

## Restricciones

- Reutilizar los datos y endpoints existentes de rutina activa, Entrenamiento de hoy, resumen semanal, objetivos y recordatorios; no crear funcionalidades de IA, Premium, social, gimnasio ni nuevos servicios remotos.
- Mostrar estados de carga, vacío y error recuperable por cada bloque sin impedir el uso del catálogo ni de Mis rutinas.
- Mantener la paleta oscura con acento verde lima y la navegación existente; el catálogo de ejercicios seguirá accesible desde el inicio.

## Listo cuando

- [x] Tras recuperar la sesión y el perfil, Android muestra un panel de inicio como pantalla inicial en lugar de abrir directamente el catálogo.
- [x] El panel muestra una tarjeta de Entrenamiento de hoy basada solo en la rutina activa, con una acción para iniciar la sesión o una orientación visible si no hay rutina activa o día programado.
- [x] El panel muestra el resumen semanal existente y el objetivo de progreso más relevante, con estados de carga, vacío, error y reintento independientes.
- [x] El panel ofrece accesos visibles a Mis rutinas, catálogo, progreso y recordatorios sin romper las rutas actuales.
- [x] La pantalla conserva estados útiles sin conexión cuando haya datos locales disponibles y explica de forma recuperable los fallos de red.
- [x] Las pruebas Android cubren los estados principales del panel, la prioridad de rutina activa y los accesos; la suite Android completa finaliza correctamente.
