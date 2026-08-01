# Objetivos de progreso Android

## Objetivo

Permitir que las personas que usan Gym App de forma independiente definan un objetivo privado y medible de peso corporal o rendimiento de entrenamiento, con fecha objetivo opcional, para interpretar sus sesiones y medidas como avances hacia una meta concreta.

## Restricciones

- Reutilizar exclusivamente el perfil, las sesiones y las medidas corporales existentes; no incluir IA, nutricion, fotos, pagos, funciones sociales, gimnasios ni cambios automaticos en las rutinas.

## Listo cuando

- [x] Una persona autenticada puede crear, editar, marcar como completado y eliminar un objetivo privado de peso corporal o de rendimiento por ejercicio desde Android.
- [x] Cada objetivo valida claramente un valor objetivo posible, una fecha opcional no pasada y la coherencia entre el tipo de objetivo y su unidad.
- [x] La pantalla Progreso muestra el objetivo activo, el valor actual calculado desde los datos existentes, la diferencia restante y el estado de avance, con carga, vacio, error recuperable y reintento.
- [x] Los objetivos se crean, consultan, modifican y eliminan mediante endpoints autenticados asociados exclusivamente a la cuenta propietaria; otra cuenta no puede leerlos ni modificarlos.
- [x] Los objetivos y su estado se conservan al cerrar y abrir la aplicacion, y no modifican rutinas, sesiones ni medidas existentes.
- [x] Las pruebas de backend y Android cubren validacion, privacidad por usuario, ciclo de vida completo y calculo de avance; una validacion en emulador confirma el flujo.

## Validacion realizada

- Backend reiniciado con la migracion `V010__progress_goals.sql` y APK debug instalado en el emulador.
- Se verificaron crear, editar, completar, eliminar y recargar objetivos despues de reiniciar la aplicacion.
- Una fecha invalida se rechaza en Android antes de crear la meta.
