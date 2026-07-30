# Temporizador de descanso de sesión Android

## Objetivo

Ayudar a los usuarios durante una sesión activa con un temporizador de descanso por serie que use el descanso configurado en su rutina, para que mantengan el ritmo de entrenamiento sin abandonar el registro de series.

## Restricciones

- Implementar únicamente en Android y reutilizar los segundos de descanso ya definidos por ejercicio en la rutina.
- El temporizador no debe modificar automáticamente series, cargas, repeticiones, rutinas ni sesiones guardadas.
- Debe continuar correctamente al rotar la pantalla o al volver brevemente desde segundo plano mientras la sesión está abierta.

## Listo cuando

- [x] Al marcar una serie como completada, la pantalla inicia un descanso con los segundos configurados para ese ejercicio.
- [x] El usuario puede pausar, reanudar, omitir y reiniciar el temporizador sin perder los datos introducidos de la sesión.
- [x] La interfaz muestra tiempo restante, ejercicio asociado y estados claros de descanso activo, pausado y terminado.
- [x] Al terminar el descanso, la aplicación muestra una señal visual y permite continuar con la siguiente serie sin cambiar ningún registro automáticamente.
- [x] El temporizador conserva los valores de descanso al recomponer, rotar o volver a la aplicación durante la misma sesión activa.
- [x] Si un ejercicio no tiene descanso configurado, no se inicia el temporizador y la sesión sigue siendo registrable.
- [x] La tarjeta del temporizador se renderiza en la parte superior de la sesión, antes de las demás series, y sus controles están disponibles sin desplazarse al final. La corrección está compilada en la APK debug.
- [x] La prueba en emulador valida iniciar una sesión real y completar una serie con la tarjeta visible. Evidencia: se mostró “Descanso · 3/4 sit-up”, “88s restantes” y los controles Pausar, Reiniciar y Omitir sobre las demás series. No hay bloqueo activo.
