# Recomendación de progresión en sesión Android

## Objetivo

Completar la entrega de recomendaciones de progresión informativas dentro de una sesión Android para que la persona vea, junto a la referencia privada de cada ejercicio, una sugerencia determinista de mantener, aumentar o reducir y decida conscientemente qué registrar en sus series. La funcionalidad Android ya está implementada; queda habilitar Docker Desktop, iniciar la base local y completar la verificación del backend.

## Restricciones

- Usar únicamente las recomendaciones de progresión y el historial privado ya disponibles para la cuenta autenticada; no usar IA, datos sociales, pagos ni recomendaciones médicas.
- La recomendación es informativa y no modifica automáticamente borradores, rutinas, referencias ni sesiones.
- Conservar la edición manual, el guardado al finalizar y el flujo sin conexión existentes.
- Para la verificación local, usar el servicio PostgreSQL definido en `infra/docker-compose.yml`, con base `gym_app`, usuario `gym_app`, contraseña `gym_app` y puerto `5432`; no cambiar estas credenciales ni desactivar Flyway para ocultar fallos.
- Resolver el bloqueo de infraestructura iniciando Docker Desktop con su motor de contenedores Linux activo antes de ejecutar Docker Compose; no sustituir PostgreSQL ni omitir la suite backend.

## Listo cuando

- [ ] Cada ejercicio de una sesión con recomendación disponible muestra su acción (mantener, aumentar o reducir), el último registro y una explicación breve basada en carga y repeticiones.
- [ ] La recomendación se muestra solo para el ejercicio correspondiente y nunca sustituye, aplica ni bloquea los campos editables de las series.
- [ ] Si no hay historial suficiente, la recomendación no está disponible o falla su carga, la sesión conserva el registro manual, las referencias existentes y un estado de error recuperable sin datos obsoletos.
- [ ] La pantalla distingue claramente entre la referencia registrada y la recomendación para la próxima ejecución antes de que la persona finalice la sesión.
- [ ] Las pruebas Android cubren recomendación disponible, historial insuficiente, error recuperable y la conservación de valores manuales, y finalizan en PASS.
- [ ] Docker Desktop está iniciado con el motor Linux disponible, y `docker version` finaliza en PASS sin un error del pipe `dockerDesktopLinuxEngine`.
- [ ] `docker compose -f infra/docker-compose.yml up -d postgres` deja PostgreSQL disponible en `localhost:5432`, y `backend\gradlew.bat -p backend test --rerun-tasks` finaliza en PASS con Flyway habilitado.
