# Frontend

## Aplicaciones móviles de socios

Ambas apps comparten diseño, contratos API y reglas de producto, pero usan UI nativa. Deben ofrecer modo degradado para registrar una sesión iniciada sin conexión y sincronizarla después sin duplicados.

### Navegación principal

- Inicio: próxima sesión, avance de meta, recordatorios y acceso a coach IA.
- Entrenar: planes, biblioteca, creador manual y generador IA.
- Progreso: historial, gráficos, medidas, fotos opcionales y análisis IA.
- Gimnasio: vínculo, membresía, QR dinámico y estado de pagos.
- Perfil: nivel, objetivos, privacidad, suscripción y ajustes.

### Criterios UX

- Registro de serie en pocos toques, con valores recientes como sugerencia editable.
- Accesibilidad: Dynamic Type/escala de fuente, lector de pantalla, contraste y estados no dependientes solo del color.
- Lenguaje claro en español de Colombia; COP con separadores locales.
- Estados vacíos que orienten hacia el siguiente paso útil.
- Errores accionables: explicar qué falló y conservar los datos introducidos.

## Panel web y app operativa

El panel web se optimiza para administración y reportes: sedes, miembros, membresías, pagos, reportes, equipo y configuración. La app operativa prioriza escáner QR, búsqueda de socio, cobro rápido y consulta de estado.

## Diseño compartido

Se definirá un sistema de diseño con tokens de color, espaciado, tipografía, iconografía, componentes y estados. Las decisiones visuales se documentarán antes de implementar pantallas para mantener paridad entre iOS, Android y web.
