# Preparación de sesión con progresión Android

## Objetivo

Completar para las personas que inician una sesión desde una rutina propia la consulta y visualización privada de sus últimas cargas y repeticiones por ejercicio, como referencia editable que no altera ningún registro antes de guardar la sesión.

## Restricciones

- Las referencias solo pueden provenir de sesiones sincronizadas de la cuenta autenticada y de ejercicios incluidos en la rutina elegida; no se muestran datos de terceros, gimnasios, IA externa, Premium ni comparaciones sociales.
- No hay bloqueo externo. La solución pendiente es añadir pruebas de integración del recurso y pruebas Android de coincidencia/estado vacío, mostrar un error recuperable con reintento, cargar referencias desde todos los accesos que inician sesión y documentar el endpoint en OpenAPI.
- Corregir, eliminar o sincronizar una sesión debe reflejarse al abrir una sesión nueva, sin reiniciar la aplicación.

## Listo cuando

- [ ] El endpoint autenticado devuelve solo la última carga y repeticiones de ejercicios pertenecientes a una rutina propia, devuelve una lista vacía sin historial y rechaza el acceso a una rutina ajena; todo queda cubierto por pruebas de backend.
- [ ] El contrato OpenAPI describe `GET /api/v1/workout-plans/{planId}/session-references`, su respuesta exitosa y sus respuestas de acceso no autorizado o prohibido.
- [ ] Iniciar una sesión desde Inicio, Rutinas o Entrenamiento de hoy solicita referencias nuevas, las asocia solo temporalmente al ejercicio correcto y no rellena ni sobrescribe carga o repeticiones editables.
- [ ] La pantalla muestra el último registro con su fecha, explica la ausencia de historial y, ante error de red, mantiene la sesión operable, elimina referencias antiguas y ofrece reintentar la consulta.
- [ ] Las pruebas Android cubren la coincidencia por ejercicio, la ausencia/error de referencias y la conservación de los campos editables sin mutación prematura.
- [ ] Las suites completas de backend y Android finalizan en PASS y el diff no contiene errores de formato.
