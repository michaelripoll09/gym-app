# Progreso de entrenamiento Android

## Objetivo

Dar a la persona autenticada una vista de progreso en Android basada en sus sesiones completadas, con un resumen de actividad reciente y volumen registrado, para que pueda entender su avance sin salir de la aplicacion.

## Restricciones

- Reutilizar GET /workout-sessions y calcular los indicadores solo con los datos de sesiones ya disponibles en Android.
- Mantener la estetica oscura con acento verde lima y una navegacion accesible desde las rutinas o el historial.
- No incluir comparativas sociales, recomendaciones automaticas, objetivos Premium, sincronizacion de dispositivos ni metricas corporales.

## Listo cuando

- [x] Una persona autenticada puede abrir Progreso desde Mis rutinas y ve estados de carga, vacio, error recuperable y contenido.
- [x] La vista muestra sesiones completadas, series registradas y repeticiones totales de los ultimos siete dias, calculadas desde las sesiones recibidas.
- [x] La vista muestra un resumen reciente por sesion con fecha, rutina y cantidad de series, conservando acceso al detalle del historial existente.
- [x] Los calculos manejan sesiones fuera de los siete dias, listas vacias y fechas invalidas sin cerrar la aplicacion ni mostrar totales incorrectos.
- [x] Las pruebas Android cubren los calculos de progreso, estados vacio y error, y la suite completa de Android finaliza correctamente.
