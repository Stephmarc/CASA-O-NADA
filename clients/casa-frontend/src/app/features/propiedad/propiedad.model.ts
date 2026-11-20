import { TipoPropiedad } from './tipoPropiedad.model';

export interface Propiedad {
  id: number;
  nombre: string;
  descripcion: string;
  precio: number;
  activo: boolean;
  stock: number;
  direccion: string;
  ciudad: string;
  areaM2: number;
  dormitorios: number;
  idVendedor?: string | null;
  tipoPropiedad: TipoPropiedad;
}
