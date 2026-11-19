import { ReservaResponse } from './reserva.model';
import { Pago } from './pago.model';

export interface PedidoConPago {
  reserva: ReservaResponse;
  pago: Pago | null;
}
