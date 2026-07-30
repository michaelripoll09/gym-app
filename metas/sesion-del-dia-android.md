# Sesión del día Android

## Objetivo

Hacer que una persona autenticada que inicia una rutina desde Entrenamiento de hoy registre únicamente las series de los ejercicios programados para el día local seleccionado, evitando que se mezclen ejercicios de otros días de la misma rutina semanal.

## Restricciones

- Mantener el comportamiento actual de iniciar una rutina desde Mis rutinas; el alcance solo modifica la entrada iniciada desde Entrenamiento de hoy.
- Reutilizar la rutina y el día ya obtenidos por GET /workout-plans; no crear tablas, endpoints ni duplicar planes.
- Conservar repeticiones y carga opcional por serie, así como el diseño oscuro con acento verde lima.
- La lógica, las pruebas y el emulador ya están disponibles. El bloqueo actual es externo: el backend no quedó escuchando en `localhost:8080`. Para recuperarlo se debe iniciar Docker Desktop, levantar PostgreSQL con `docker compose -f infra/docker-compose.yml up -d`, abrir una terminal en `backend`, definir `GYM_JWT_SECRET` con el valor de desarrollo configurado y ejecutar `./gradlew.bat bootRun`; continuar solo cuando `Get-NetTCPConnection -LocalPort 8080 -State Listen` devuelva un proceso activo.

## Listo cuando

- [ ] Al iniciar una rutina desde Entrenamiento de hoy, el borrador de sesión contiene únicamente los ejercicios y series del día local mostrado.
- [ ] Una rutina con varios días no muestra ni envía ejercicios de días distintos al día elegido al finalizar una sesión diaria.
- [ ] El inicio desde Mis rutinas conserva su comportamiento actual y sigue creando un borrador con todos los días de la rutina.
- [ ] El flujo diario conserva la validación de repeticiones y carga opcional por serie antes de guardar.
- [ ] Las pruebas Android cubren rutina de un día, rutina de varios días y la diferencia entre iniciar desde hoy e iniciar desde Mis rutinas.
- [ ] La suite completa de Android finaliza correctamente antes de la comprobación manual.
- [ ] Con backend disponible, el emulador comprueba una rutina programada para hoy con otro día adicional y muestra en la sesión solamente las series de hoy.
