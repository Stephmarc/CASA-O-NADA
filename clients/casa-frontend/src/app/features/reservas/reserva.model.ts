export interface DetalleReservaRequest {
  idPropiedad: number;
  cantidad: number;
}

export interface ReservaRequest {
  metodoPago: string;
  detalles: DetalleReservaRequest[];
}

export interface DetalleReservaResponse {
  idPropiedad: number;
  nombrePropiedad: string | null;
  cantidad: number;
  precioUnitario: number | null;
  subtotal: number | null;
}

export interface ReservaResponse {
  id: number;
  idCliente: string;
  fechaCreacion: string;
  estado: string;
  total: number | null;
  detalles: DetalleReservaResponse[];
}
