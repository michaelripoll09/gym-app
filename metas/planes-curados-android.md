# Planes curados Android

## Objetivo

Permitir que personas autenticadas, especialmente quienes aún no saben crear una rutina, descubran planes curados compatibles con su perfil principal, nivel y objetivo, revisen su programación semanal y adopten uno como una copia editable dentro de sus rutinas personales.

## Restricciones

- El bloque cubre Android y la API necesaria para planes curados; no incluye IA, pagos, gimnasios ni cambia las rutinas personales ya creadas.
- Los ejercicios de cada plan deben provenir exclusivamente del catálogo publicado y ser compatibles con el perfil principal del usuario.
- Adoptar un plan debe crear una copia personal editable; nunca debe modificar la plantilla curada ni activar una sesión automáticamente.

## Listo cuando

- [ ] La API devuelve solo planes curados publicados compatibles con el perfil principal, nivel y objetivo del usuario autenticado.
- [ ] Cada plan curado expone nombre, descripción breve, días, ejercicios, series, rango de repeticiones y descanso, usando ejercicios publicados del catálogo.
- [ ] Android muestra desde el catálogo una entrada a Planes recomendados con estados de carga, vacío, error y reintento.
- [ ] La persona puede abrir un plan, revisar su programación semanal y confirmar Usar esta rutina.
- [ ] La confirmación crea una rutina personal editable con los mismos días y ejercicios y la muestra en Mis rutinas, sin alterar la plantilla curada.
- [ ] La API rechaza acceso o adopción de planes no compatibles y las pruebas backend y Android cubren compatibilidad, copia y estados de interfaz.
- [ ] La suite backend, la suite Android y el emulador verifican que un perfil de prueba adopta una plantilla compatible y puede editar su copia.
