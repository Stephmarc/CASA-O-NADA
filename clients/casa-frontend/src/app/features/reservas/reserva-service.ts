import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ReservaRequest, ReservaResponse } from './reserva.model';

@Injectable({ providedIn: 'root' })
export class ReservaService {
  private readonly http = inject(HttpClient);

  crear(request: ReservaRequest): Observable<ReservaResponse> {
    return this.http.post<ReservaResponse>(`${environment.apiBaseUrl}/api/v1/reservas`, request);
  }

  obtener(id: number): Observable<ReservaResponse> {
    return this.http.get<ReservaResponse>(`${environment.apiBaseUrl}/api/v1/reservas/${id}`);
  }

  listar(): Observable<ReservaResponse[]> {
    return this.http.get<ReservaResponse[]>(`${environment.apiBaseUrl}/api/v1/reservas`);
  }
}
