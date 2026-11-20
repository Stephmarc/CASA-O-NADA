import { Component, inject, signal, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth/auth-service';
import { environment } from '../../../environments/environment';
import { FotoPropiedad } from './foto-propiedad';
import { Propiedad } from './propiedad.model';
@Component({
  selector: 'app-propiedades',
  imports: [FormsModule, RouterLink, FotoPropiedad],
  template: `
    <section class="hero">
      <div class="hero-in">
        <h1>Encuentra la casa que buscas</h1>
        <p>Explora las propiedades disponibles y sepárala con un pago de reserva.</p>
        <div class="buscador">
          <input [(ngModel)]="busqueda" placeholder="Busca por nombre, dirección o ciudad" />
          <select [(ngModel)]="ciudad">
            <option value="">Todas las ciudades</option>
            @for (c of listaCiudades(); track c) {
              <option [value]="c">{{ c }}</option>
            }
          </select>
          <span class="buscador-cifra">{{ filtradas().length }} {{ filtradas().length === 1 ? 'resultado' : 'resultados' }}</span>
        </div>
        <ul class="ventajas">
          <li><svg viewBox="0 0 24 24"><path d="M22 11.1V12a10 10 0 1 1-5.9-9.1"/><path d="M22 4L12 14l-3-3"/></svg>Reserva en línea</li>
          <li><svg viewBox="0 0 24 24"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg>Pago con confirmación inmediata</li>
          <li><svg viewBox="0 0 24 24"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><path d="M9 22V12h6v10"/></svg>Fichas con área y dormitorios</li>
        </ul>
      </div>
    </section>
    <div class="resultados">
    <div class="filtros">
      <div class="chips">
        <button class="chip" [class.activo]="tipo === ''" (click)="tipo = ''">Todos</button>
        @for (t of tipos(); track t.id) {
          <button class="chip" [class.activo]="tipo === t.nombre" (click)="tipo = t.nombre">{{ t.nombre }}</button>
        }
      </div>
      <div class="selectores">
        <select [(ngModel)]="precioMax" aria-label="Precio máximo">
          <option [ngValue]="0">Cualquier precio</option>
          <option [ngValue]="1000">Hasta S/ 1000</option>
          <option [ngValue]="1500">Hasta S/ 1500</option>
          <option [ngValue]="2000">Hasta S/ 2000</option>
        </select>
        <select [(ngModel)]="dormMin" aria-label="Dormitorios">
          <option [ngValue]="0">Cualquier dormitorio</option>
          <option [ngValue]="2">2 o más</option>
          <option [ngValue]="3">3 o más</option>
          <option [ngValue]="4">4 o más</option>
        </select>
        <select [(ngModel)]="orden" aria-label="Ordenar">
          <option value="">Ordenar por</option>
          <option value="asc">Precio: menor a mayor</option>
          <option value="desc">Precio: mayor a menor</option>
        </select>
      </div>
    </div>
    @if (mensaje()) {
      <p role="status">{{ mensaje() }}</p>
    }
    <div class="resultados-titulo"><h2>Propiedades</h2></div>
    <div class="property-grid">
      @for (p of filtradas(); track p.id) {
        <article class="property-card">
          <div class="card-foto">
            <a [routerLink]="['/propiedad', p.id]"><app-foto-propiedad [tipo]="p.tipoPropiedad?.nombre ?? ''" /></a>
            <span class="estado" [class.agotada]="p.stock !== 1">{{ p.stock === 1 ? 'Disponible' : 'Reservada' }}</span>
          </div>
          <div class="card-cuerpo">
            <div class="precio">S/ {{ p.precio }} <small>de reserva</small></div>
            <h3><a [routerLink]="['/propiedad', p.id]">{{ p.nombre }}</a></h3>
            <p class="ubicacion">{{ p.direccion }}, {{ p.ciudad }}</p>
            <div class="datos">
              <span>{{ p.tipoPropiedad?.nombre }}</span>
              <span>{{ p.areaM2 }} m²</span>
              <span>{{ p.dormitorios }} dorm.</span>
            </div>
            <div class="acciones">
              @if (!auth.autenticado() || auth.tieneRol('CLIENTE')) {
                <a class="button" routerLink="/reservas/nueva">Reservar</a>
              }
              @if (puedeGestionar(p)) {
                <button class="secundario" (click)="editar(p)">Editar</button>
                <button class="peligro" (click)="eliminar(p.id)">Eliminar</button>
              }
            </div>
          </div>
        </article>
      }
    </div>
    <section class="ciudades">
      <h2>Explora por ciudad</h2>
      <div class="ciudades-grid">
        @for (c of ciudadesConConteo(); track c.nombre) {
          <a class="ciudad" [routerLink]="['/']" [queryParams]="{ ciudad: c.nombre }" [style.background-image]="c.fondo">
            <strong>{{ c.nombre }}</strong>
            <span>{{ c.cantidad }} {{ c.cantidad === 1 ? 'propiedad' : 'propiedades' }}</span>
          </a>
        }
      </div>
    </section>

    <section class="beneficios">
      <div>
        <span class="paso-icono"><svg viewBox="0 0 24 24"><rect x="2" y="5" width="20" height="14" rx="2"/><path d="M2 10h20"/></svg></span>
        <h3>Reserva sin salir de casa</h3>
        <p>Separa tu propiedad en línea y recibe la confirmación del pago al instante.</p>
      </div>
      <div>
        <span class="paso-icono"><svg viewBox="0 0 24 24"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><path d="M9 22V12h6v10"/></svg></span>
        <h3>Información completa</h3>
        <p>Cada propiedad muestra su dirección, área, dormitorios y el importe de reserva.</p>
      </div>
      <div>
        <span class="paso-icono"><svg viewBox="0 0 24 24"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg></span>
        <h3>Tu reserva protegida</h3>
        <p>Si el pago no se aprueba, la propiedad vuelve a quedar disponible para otros clientes.</p>
      </div>
    </section>

    @if (!auth.autenticado()) {
      <section class="llamado">
        <div>
          <h2>¿Tienes una propiedad para ofrecer?</h2>
          <p>Publícala en CASA O NADA y llega a más clientes.</p>
        </div>
        <button type="button" (click)="auth.iniciarLogin()">Iniciar sesión</button>
      </section>
    }
    @if (auth.tieneRol('ADMIN', 'VENDEDOR')) {
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
    </div>
  `,
})
export class Propiedades implements OnInit {
  protected auth = inject(AuthService);
  private http = inject(HttpClient);
  private ruta = inject(ActivatedRoute);
  private base = environment.apiBaseUrl + '/api/v1';
  protected propiedades = signal<any[]>([]);
  protected tipos = signal<any[]>([]);
  protected mensaje = signal('');
  protected busqueda = '';
  protected ciudad = '';
  protected tipo = '';
  protected precioMax = 0;
  protected dormMin = 0;
  protected orden = '';
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
    // Los enlaces del pie y de "Explora por ciudad" llegan como ?tipo=Casa o ?ciudad=Lima
    this.ruta.queryParamMap.subscribe((q) => {
      this.tipo = q.get('tipo') ?? '';
      this.ciudad = q.get('ciudad') ?? '';
      if (q.get('tipo') || q.get('ciudad')) setTimeout(() => document.querySelector('.filtros')?.scrollIntoView({ behavior: 'smooth', block: 'center' }));
    });
    this.cargar();
    this.http.get<any[]>(this.base + '/tipos-propiedad').subscribe({
      next: (x) => this.tipos.set(x),
      error: () => this.mensaje.set('No se pudieron cargar los tipos de propiedad.'),
    });
  }
  cargar() {
    this.http.get<any[]>(this.base + '/propiedades').subscribe({
      next: (x) => this.propiedades.set(x.filter((p) => p.activo)), // las inactivas no se muestran
      error: () => this.mensaje.set('No se pudo conectar con el servicio de propiedades.'),
    });
  }
  filtradas() {
    const texto = this.busqueda.toLowerCase();
    const lista = this.propiedades().filter(
      (p) =>
        (p.nombre + ' ' + p.direccion + ' ' + p.ciudad).toLowerCase().includes(texto) &&
        (!this.ciudad || p.ciudad === this.ciudad) &&
        (!this.tipo || p.tipoPropiedad?.nombre === this.tipo) &&
        (!this.precioMax || p.precio <= this.precioMax) &&
        p.dormitorios >= this.dormMin,
    );
    if (this.orden) lista.sort((a, b) => (this.orden === 'asc' ? a.precio - b.precio : b.precio - a.precio));
    return lista;
  }
  // Ciudades con más propiedades primero; la foto es la de la primera propiedad de esa ciudad
  ciudadesConConteo() {
    return this.listaCiudades()
      .map((nombre) => {
        const deCiudad = this.propiedades().filter((p) => p.ciudad === nombre);
        const tipo = (deCiudad[0]?.tipoPropiedad?.nombre ?? 'casa').toLowerCase();
        return { nombre, cantidad: deCiudad.length, fondo: `linear-gradient(180deg, rgba(18,55,47,.15), rgba(18,55,47,.85)), url('/images/${tipo}-referencia.jpg')` };
      })
      .sort((a, b) => b.cantidad - a.cantidad)
      .slice(0, 5);
  }
  listaCiudades() {
    return [...new Set(this.propiedades().map((p) => p.ciudad))].sort();
  }
  // ADMIN gestiona todas; VENDEDOR solo las propias (el backend lo vuelve a validar)
  puedeGestionar(p: Propiedad): boolean {
    return this.auth.tieneRol('ADMIN') || (this.auth.tieneRol('VENDEDOR') && p.idVendedor === this.auth.idCliente());
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
