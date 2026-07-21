# Flujos funcionales

## Usuario independiente

1. Descarga la app, crea cuenta y acepta términos y política de privacidad.
2. Completa perfil: nivel editable, objetivo, disponibilidad, equipamiento y restricciones no médicas.
3. Elige un plan curado, crea una rutina o pide una propuesta a la IA.
4. Revisa y confirma la rutina antes de usarla.
5. Durante la sesión registra ejecución y percepción del esfuerzo.
6. Consulta progreso y recibe recomendaciones contextualizadas.
7. Decide mantener el plan Free o activar Premium desde las tiendas móviles.

## Rutina generada con IA

1. El usuario indica objetivo, nivel, días, duración, equipamiento y preferencias.
2. El backend valida que los datos estén dentro de los límites seguros del producto.
3. La IA devuelve una propuesta estructurada: días, ejercicios, series, repeticiones, descanso, progresión y explicación.
4. Se valida contra el catálogo de ejercicios y reglas de seguridad.
5. El usuario edita o acepta; la rutina nunca se activa sin confirmación.
6. El historial de cambios permite explicar recomendaciones posteriores.

## Vinculación a un gimnasio

1. El dueño crea organización, sede y plan de membresía.
2. Invita al socio o le entrega un código/QR de vinculación.
3. El socio acepta el vínculo en su cuenta.
4. La plataforma crea la relación socio-sede y aplica la membresía correspondiente.
5. La app muestra un QR dinámico mientras la membresía esté activa.

## Acceso y pagos del gimnasio

1. Recepción escanea el QR dinámico desde la aplicación operativa.
2. El backend verifica firma, vigencia, sede, membresía y posibles bloqueos.
3. Se registra el acceso o rechazo con motivo y auditoría.
4. El personal registra efectivo o transferencia, o inicia un cobro digital.
5. El proveedor de pagos comunica el resultado mediante webhook verificado.
6. El sistema concilia el pago, emite comprobante y actualiza la membresía.

## Privacidad

- El socio puede desvincularse de una sede; esto no elimina su cuenta ni sus datos personales.
- Las fotos de progreso son privadas y pueden eliminarse desde el perfil.
- Un entrenador solo recibe datos de entrenamiento cuando el usuario lo autoriza de forma explícita y revocable.
