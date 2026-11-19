import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { TipoPropiedad } from './tipoPropiedad.model';

@Injectable({ providedIn: 'root' })
export class TipoPropiedadService {
  private readonly http = inject(HttpClient);

  listar(): Observable<TipoPropiedad[]> {
    return this.http.get<TipoPropiedad[]>(`${environment.apiBaseUrl}/api/v1/tipos-propiedad`);
  }

  obtener(id: number): Observable<TipoPropiedad> {
    return this.http.get<TipoPropiedad>(`${environment.apiBaseUrl}/api/v1/tipos-propiedad/${id}`);
  }

  crear(tipoPropiedad: TipoPropiedad): Observable<TipoPropiedad> {
    return this.http.post<TipoPropiedad>(
      `${environment.apiBaseUrl}/api/v1/tipos-propiedad`,
      tipoPropiedad,
    );
  }

  actualizar(id: number, tipoPropiedad: TipoPropiedad): Observable<TipoPropiedad> {
    return this.http.put<TipoPropiedad>(
      `${environment.apiBaseUrl}/api/v1/tipos-propiedad/${id}`,
      tipoPropiedad,
    );
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${environment.apiBaseUrl}/api/v1/tipos-propiedad/${id}`);
  }
}
