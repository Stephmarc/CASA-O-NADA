import { Component, inject, signal, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth/auth-service';
import { environment } from '../../../environments/environment';
import { FotoPropiedad } from './foto-propiedad';
@Component({
  selector: 'app-propiedades',
  imports: [FormsModule, RouterLink, FotoPropiedad],
  template: `
    <div class="eyebrow">ENCUENTRA TU PRÓXIMO ESPACIO</div>
    <h1>Una casa. Un nuevo comienzo.</h1>
    <p>
      Explora propiedades y separa la que te interesa. El importe corresponde a la reserva
      académica.
    </p>
    <label
      >Buscar por nombre o ciudad
      <input [(ngModel)]="busqueda" placeholder="Lima, casa, departamento…"
    /></label>
    @if (mensaje()) {
      <p role="status">{{ mensaje() }}</p>
    }
    <div class="property-grid">
      @for (p of filtradas(); track p.id) {
        <article class="property-card">
          <app-foto-propiedad [tipo]="p.tipoPropiedad?.nombre ?? ''" />
          <span class="eyebrow">{{ p.tipoPropiedad?.nombre }} · {{ p.ciudad }}</span>
          <h2>{{ p.nombre }}</h2>
          <p>{{ p.descripcion }}</p>
          <p>{{ p.direccion }}</p>
          <p>{{ p.areaM2 }} m² · {{ p.dormitorios }} dormitorios</p>
          <strong>S/ {{ p.precio }} por reserva</strong>
          <p>{{ p.stock === 1 ? 'Disponible' : 'Reservada' }}</p>
          <a class="button" routerLink="/reservas/nueva">Ver reserva</a>
          @if (auth.tieneRol('ADMIN')) {
            <button (click)="editar(p)">Editar</button
            ><button (click)="eliminar(p.id)">Eliminar</button>
          }
        </article>
      }
    </div>
    @if (auth.tieneRol('ADMIN')) {
      <section class="panel">
        <h2>{{ form.id ? 'Editar propiedad' : 'Nueva propiedad' }}</h2>
        <form (ngSubmit)="guardar()">
          <label
            >Nombre<input name="nombre" [(ngModel)]="form.nombre" required maxlength="100"
          /></label>
          <label
            >Descripción<input name="descripcion" [(ngModel)]="form.descripcion" maxlength="255"
          /></label>
          <label>Dirección<input name="direccion" [(ngModel)]="form.direccion" required /></label>
          <label>Ciudad<input name="ciudad" [(ngModel)]="form.ciudad" required /></label>
          <label
            >Área (m²)<input name="area" type="number" [(ngModel)]="form.areaM2" min="1" required
          /></label>
          <label
            >Dormitorios<input
              name="dormitorios"
              type="number"
              [(ngModel)]="form.dormitorios"
              min="0"
              required
          /></label>
          <label
            >Importe de reserva (S/)<input
              name="precio"
              type="number"
              [(ngModel)]="form.precio"
              min="1"
              required
          /></label>
          <label
            >Tipo<select name="tipo" [(ngModel)]="form.tipoPropiedadId">
              @for (t of tipos(); track t.id) {
                <option [ngValue]="t.id">{{ t.nombre }}</option>
              }
            </select></label
          >
          <label
            >Disponibilidad<select name="stock" [(ngModel)]="form.stock">
              <option [ngValue]="1">Disponible</option>
              <option [ngValue]="0">Reservada</option>
            </select></label
          >
          <label><input name="activo" type="checkbox" [(ngModel)]="form.activo" />Publicada</label
          ><button type="submit">Guardar propiedad</button
          ><button type="button" (click)="form = nueva()">Limpiar</button>
        </form>
      </section>
    }
  `,
})
export class Propiedades implements OnInit {
  protected auth = inject(AuthService);
  private http = inject(HttpClient);
  private base = environment.apiBaseUrl + '/api/v1';
  protected propiedades = signal<any[]>([]);
  protected tipos = signal<any[]>([]);
  protected mensaje = signal('');
  protected busqueda = '';
  protected form: any = this.nueva();
  nueva() {
    return {
      nombre: '',
      descripcion: '',
      direccion: '',
      ciudad: 'Lima',
      areaM2: 100,
      dormitorios: 2,
      precio: 1000,
      stock: 1,
      activo: true,
      tipoPropiedadId: 1,
    };
  }
  ngOnInit() {
    this.cargar();
    this.http.get<any[]>(this.base + '/tipos-propiedad').subscribe({
      next: (x) => this.tipos.set(x),
      error: () => this.mensaje.set('No se pudieron cargar los tipos de propiedad.'),
    });
  }
  cargar() {
    this.http.get<any[]>(this.base + '/propiedades').subscribe({
      next: (x) => this.propiedades.set(x),
      error: () => this.mensaje.set('No se pudo conectar con el servicio de propiedades.'),
    });
  }
  filtradas() {
    return this.propiedades().filter((p) =>
      (p.nombre + ' ' + p.ciudad).toLowerCase().includes(this.busqueda.toLowerCase()),
    );
  }
  editar(p: any) {
    this.form = { ...p, tipoPropiedadId: p.tipoPropiedad.id };
  }
  guardar() {
    let id = this.form.id;
    const req = id
      ? this.http.put(this.base + '/propiedades/' + id, this.form)
      : this.http.post(this.base + '/propiedades', this.form);
    req.subscribe({
      next: () => {
        this.mensaje.set('Propiedad guardada');
        this.form = this.nueva();
        this.cargar();
      },
      error: () => this.mensaje.set('Revisa los campos y los permisos de tu cuenta.'),
    });
  }
  eliminar(id: number) {
    if (!confirm('¿Eliminar esta propiedad?')) return;
    this.http.delete(this.base + '/propiedades/' + id).subscribe({
      next: () => this.cargar(),
      error: () => this.mensaje.set('No se pudo eliminar la propiedad.'),
    });
  }
}
