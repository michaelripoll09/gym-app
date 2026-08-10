# Records personales Android

## Objetivo

Mostrar a cada persona sus records personales por ejercicio a partir de sus sesiones completadas, para que pueda reconocer mejoras reales de fuerza y repeticiones desde la pantalla de progreso.

## Restricciones

- Los records se calculan solo con sesiones sincronizadas y pertenecientes al usuario autenticado; las sesiones pendientes sin conexion no se incluyen.
- Para cada ejercicio se muestran por separado la mayor carga registrada y el mayor numero de repeticiones registrado, junto con la fecha de la marca.
- Los empates conservan la marca mas reciente y no modifican el historial, la rutina ni las series originales.
- Una correccion, eliminacion o sincronizacion posterior debe recalcular los records mostrados sin reiniciar la aplicacion.
- Este bloque no incorpora comparaciones sociales, clasificaciones publicas, funciones premium ni consejos medicos.

## Listo cuando

- [ ] El backend expone un recurso autenticado de records personales que solo devuelve agregados de sesiones propias y no revela datos de otros usuarios.
- [ ] El calculo devuelve por ejercicio el record de carga y el record de repeticiones con su fecha, aplica los empates definidos y excluye sesiones no disponibles para el usuario.
- [ ] La pantalla de progreso Android muestra estados de carga, vacio y error recuperable, y presenta los records por ejercicio de forma legible.
- [ ] Los records se actualizan tras corregir, eliminar o sincronizar una sesion, sin requerir cerrar ni reiniciar la aplicacion.
- [ ] Las pruebas de backend y Android cubren propiedad, calculo, empates, estados de interfaz y refresco, y las suites correspondientes finalizan en PASS.
