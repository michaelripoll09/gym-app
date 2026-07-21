# Pagos y monetización

## Premium personal

- La compra se realiza mediante Apple App Store y Google Play, de acuerdo con las políticas vigentes de cada tienda.
- El backend verifica transacciones, mantiene el estado de entitlement y gestiona restauración de compras.
- Los planes iniciales serán mensual y anual en COP, con precios configurables sin actualizar las apps.
- Al vencer, se conservan rutinas y datos; se restringen únicamente capacidades Premium.

## Membresías de gimnasio

- El personal puede registrar pagos de efectivo y transferencia con método, monto, fecha, referencia y comprobante opcional.
- Los pagos en línea se crean mediante un adaptador de proveedor colombiano; el resultado definitivo llega por webhook firmado.
- Cada pago tiene estados: pendiente, aprobado, rechazado, anulado o reembolsado.
- La activación, renovación o suspensión de membresía se registra de forma trazable.

## Controles

- Los importes se calculan en servidor.
- Las operaciones financieras usan claves de idempotencia.
- Los webhooks se validan, almacenan y procesan una sola vez.
- El sistema separa el dinero de cada sede y no almacena datos de tarjeta.
