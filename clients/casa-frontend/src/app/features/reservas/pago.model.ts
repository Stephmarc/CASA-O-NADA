export interface Pago {
  id: number;
  reservaId: number;
  monto: number;
  metodoPago: string;
  estado: string;
  fechaPago: string;
}
