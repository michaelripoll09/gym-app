# Análisis de progreso Android

## Objetivo

Ofrecer a cada persona un análisis privado y accionable de su evolución reciente a partir de sus sesiones, adherencia, récords, medidas y metas, para que entienda su progreso sin requerir un gimnasio ni interpretar manualmente datos dispersos.

## Restricciones

- El análisis se calcula exclusivamente con los datos autenticados y sincronizados de la propia cuenta, sin compartir rutinas, medidas, historial ni metas con gimnasios u otros usuarios.
- Las conclusiones se basan en reglas transparentes y muestran sus datos de origen; no diagnostican, no prescriben tratamiento, no sustituyen profesionales de salud y no incorporan chat, modelos de IA externos, Premium, pagos ni comparaciones sociales.
- Si faltan datos suficientes, Android debe explicarlo de forma clara y orientar a registrar sesiones, medidas o metas, sin inventar resultados.
- Corregir, eliminar o sincronizar sesiones y modificar medidas o metas debe recalcular el análisis al volver a consultarlo.

## Listo cuando

- [ ] El backend expone un recurso autenticado que resume de forma privada el periodo reciente con sesiones completadas, adherencia, cambios de medidas disponibles, avance de metas y récords personales nuevos.
- [ ] El resultado identifica de manera verificable sus fuentes y devuelve estados explícitos cuando no existe información suficiente, sin revelar datos de otra cuenta.
- [ ] Android presenta un análisis legible desde Progreso, incluyendo carga, estado vacío, error recuperable y una forma de volver a consultar los datos.
- [ ] Las actualizaciones de sesiones, sincronización sin conexión, medidas y metas refrescan el análisis mostrado sin exigir reiniciar la aplicación.
- [ ] Las pruebas de backend y Android cubren propiedad, cálculo con datos y sin datos, estados de interfaz y refrescos, y las suites correspondientes finalizan en PASS.
