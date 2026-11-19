import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Pago } from './pago.model';

@Injectable({ providedIn: 'root' })
export class PagoService {
  private readonly http = inject(HttpClient);

  obtenerPorReserva(reservaId: number): Observable<Pago> {
    return this.http.get<Pago>(`${environment.apiBaseUrl}/api/v1/pagos/por-reserva/${reservaId}`);
  }

  listar(): Observable<Pago[]> {
    return this.http.get<Pago[]>(`${environment.apiBaseUrl}/api/v1/pagos`);
  }
}
