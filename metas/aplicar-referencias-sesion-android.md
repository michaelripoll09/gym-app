# Aplicar referencias de sesión Android

## Objetivo

Permitir a la persona que prepara una sesión usar voluntariamente su última referencia privada de carga y repeticiones como punto de partida para las series pendientes de un ejercicio, reduciendo la entrada manual sin quitarle control sobre lo que finalmente registra.

## Restricciones

- La acción solo usa referencias de la cuenta autenticada ya cargadas para la rutina actual; no usa IA, datos sociales, gimnasios ni funciones Premium.
- No se completa ningún campo automáticamente al abrir la sesión: cada aplicación requiere una acción explícita de la persona y los valores continúan siendo editables.
- No se crea ni actualiza ninguna sesión hasta que la persona pulse finalizar y el flujo existente de trabajo sin conexión debe conservarse.

## Listo cuando

- [ ] Cada ejercicio con referencia muestra una acción clara para aplicar sus repeticiones y carga a las series aún vacías de ese mismo ejercicio.
- [ ] La acción no modifica ejercicios distintos, series que la persona ya completó manualmente ni campos sin una referencia disponible.
- [ ] Después de aplicar una referencia, los valores quedan visibles y editables antes de guardar la sesión.
- [ ] Un error, ausencia de historial o reintento de referencias conserva el flujo actual de registro y no habilita acciones con datos obsoletos.
- [ ] Las pruebas Android cubren aplicación selectiva, conservación de valores manuales y ausencia de referencia; las suites Android y backend finalizan en PASS.
