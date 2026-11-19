import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Propiedad } from './propiedad.model';

@Injectable({ providedIn: 'root' })
export class PropiedadService {
  private readonly http = inject(HttpClient);

  listar(): Observable<Propiedad[]> {
    return this.http.get<Propiedad[]>(`${environment.apiBaseUrl}/api/v1/propiedades`);
  }
}
