# Rutina activa Android

## Objetivo

Permitir que una persona autenticada elija una única rutina personal como activa para que Entrenamiento de hoy, los recordatorios y el progreso semanal se orienten a su programación actual, sin tener que archivar ni eliminar sus demás rutinas.

## Restricciones

- La selección de rutina activa se guarda por cuenta en el backend y se recupera al iniciar sesión; una persona puede tener cero o una rutina activa.
- Solo pueden activarse rutinas propias no archivadas; no se modifican ejercicios, días, historial, sesiones ni planes curados.
- Android debe ofrecer el cambio desde Mis rutinas, identificar claramente la rutina activa y manejar carga, estado vacío y error recuperable.
- Entrenamiento de hoy y los recordatorios usan la rutina activa cuando exista; si no existe, conservan el comportamiento actual de mostrar las rutinas disponibles y orientar a seleccionar una.
- Quedan fuera de alcance IA, Premium, calendarios externos, compartir rutinas y cambios automáticos de rutina activa.
- Validación visual resuelta: se completó onboarding con una disciplina principal, se creó una rutina guiada, se activó desde Mis rutinas y Entrenamiento de hoy mostró únicamente sus ejercicios programados.

## Listo cuando

- [x] La API persiste, consulta y cambia la rutina activa de la cuenta, rechazando rutinas archivadas, inexistentes o de otra persona.
- [x] Mis rutinas permite activar otra rutina, muestra una sola como Activa y conserva correctamente ese estado al recargar la aplicación.
- [x] Entrenamiento de hoy prioriza exclusivamente los ejercicios programados de la rutina activa y muestra una orientación clara cuando no existe una.
- [x] Los recordatorios locales se reprograman al cambiar la rutina activa y solo consideran sus días programados.
- [x] El emulador valida de extremo a extremo activar una rutina y abrir su entrenamiento del día después de completar el onboarding; backend y Android mantienen sus pruebas automatizadas en PASS.
