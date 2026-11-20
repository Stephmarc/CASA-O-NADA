import { Component, OnInit, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth/auth-service';
import { environment } from '../../../environments/environment';
import { FotoPropiedad } from './foto-propiedad';
import { Propiedad } from './propiedad.model';

// Página de una propiedad: foto, datos principales, caja de reserva y propiedades similares.
@Component({
  selector: 'app-detalle',
  imports: [RouterLink, FotoPropiedad],
  template: `
    <a routerLink="/" class="volver">← Volver a propiedades</a>

    @if (propiedad(); as p) {
      <div class="detalle-grid">
        <div>
          <div class="detalle-foto">
            <app-foto-propiedad [tipo]="p.tipoPropiedad?.nombre ?? ''" />
          </div>
          <span class="etiqueta">{{ p.tipoPropiedad?.nombre }}</span>
          <h1>{{ p.nombre }}</h1>
          <p class="ubicacion">{{ p.direccion }}, {{ p.ciudad }}</p>

          <div class="specs">
            <div><strong>{{ p.areaM2 }} m²</strong><span>Área</span></div>
            <div><strong>{{ p.dormitorios }}</strong><span>Dormitorios</span></div>
            <div><strong>{{ p.tipoPropiedad?.nombre }}</strong><span>Tipo</span></div>
            <div><strong>{{ p.ciudad }}</strong><span>Ciudad</span></div>
          </div>

          <h2>Descripción</h2>
          <p>{{ p.descripcion }}</p>
        </div>

        <aside class="reserva-box">
          <span class="estado" [class.agotada]="p.stock !== 1">{{ p.stock === 1 ? 'Disponible' : 'Reservada' }}</span>
          <div class="precio">S/ {{ p.precio }}</div>
          <p>Importe de reserva. Separa la propiedad con un pago y sigue el resultado en "Mis reservas".</p>
          @if (p.stock === 1 && (!auth.autenticado() || auth.tieneRol('CLIENTE'))) {
            <a class="button" routerLink="/reservas/nueva" [queryParams]="{ propiedad: p.id }">Reservar ahora</a>
          } @else if (p.stock !== 1) {
            <button disabled>No disponible</button>
          }
          <ul>
            <li>Pago seguro simulado</li>
            <li>Confirmación en segundos</li>
            <li>Si el pago falla, la propiedad vuelve a estar disponible</li>
          </ul>
        </aside>
      </div>

      @if (similares().length) {
        <h2 class="similares-titulo">Propiedades similares</h2>
        <div class="property-grid">
          @for (s of similares(); track s.id) {
            <article class="property-card">
              <a [routerLink]="['/propiedad', s.id]" class="card-foto">
                <app-foto-propiedad [tipo]="s.tipoPropiedad?.nombre ?? ''" />
              </a>
              <div class="card-cuerpo">
                <div class="precio">S/ {{ s.precio }} <small>de reserva</small></div>
                <h3>{{ s.nombre }}</h3>
                <p class="ubicacion">{{ s.direccion }}, {{ s.ciudad }}</p>
              </div>
            </article>
          }
        </div>
      }
    } @else if (error()) {
      <p role="alert">{{ error() }}</p>
    }
  `,
})
export class Detalle implements OnInit {
  protected auth = inject(AuthService);
  private http = inject(HttpClient);
  private ruta = inject(ActivatedRoute);
  private api = environment.apiBaseUrl + '/api/v1/propiedades';

  protected propiedad = signal<Propiedad | null>(null);
  protected similares = signal<Propiedad[]>([]);
  protected error = signal('');

  ngOnInit() {
    // Si cambia el id en la URL (al pulsar una propiedad similar) se vuelve a cargar
    this.ruta.paramMap.subscribe((params) => this.cargar(Number(params.get('id'))));
  }

  private cargar(id: number) {
    this.http.get<Propiedad>(`${this.api}/${id}`).subscribe({
      next: (p) => {
        this.propiedad.set(p);
        window.scrollTo(0, 0);
        this.http.get<Propiedad[]>(this.api).subscribe((todas) =>
          this.similares.set(
            todas.filter((x) => x.activo && x.id !== id && x.tipoPropiedad?.id === p.tipoPropiedad?.id).slice(0, 3),
          ),
        );
      },
      error: () => this.error.set('No se encontró la propiedad.'),
    });
  }
}
