# Generador guiado de rutinas Android

## Objetivo

Permitir que una persona independiente obtenga una propuesta de rutina inicial personalizada desde su perfil, objetivo, disponibilidad y catálogo validado, la revise con una explicación clara y decida si la crea en su cuenta antes de modificar sus rutinas existentes.

## Restricciones

- La recomendación solo puede usar ejercicios publicados del catálogo y debe respetar perfil principal, nivel, objetivo, días y duración del perfil.
- El backend concentra la lógica de recomendación y cualquier proveedor de IA; Android nunca guarda claves de IA ni llama directamente a un proveedor externo.
- La propuesta no se guarda ni reemplaza una rutina hasta que la persona la confirma; debe existir una alternativa segura si la recomendación no está disponible.
- Excluir chat conversacional, suscripciones Premium, análisis médico, gimnasios y pagos.

## Listo cuando

- [ ] Android ofrece desde el catálogo una entrada para generar una rutina guiada y muestra estados de carga, error recuperable y propuesta disponible.
- [ ] El backend genera una propuesta estructurada usando el perfil de la persona y ejercicios publicados, con nombre, días, series, repeticiones, descansos y explicación breve.
- [ ] La propuesta respeta la disponibilidad declarada y rechaza o explica una combinación incompatible sin crear una rutina inválida.
- [ ] La persona puede confirmar la propuesta para crear su rutina o descartarla sin alterar rutinas existentes.
- [ ] La configuración del proveedor de IA, si se habilita, queda solo en variables de entorno y existe un fallback determinista verificable para desarrollo.
- [ ] Pruebas backend y Android cubren propuesta válida, incompatibilidad, fallo del proveedor, confirmación y descarte; el emulador valida generar y confirmar una rutina.
