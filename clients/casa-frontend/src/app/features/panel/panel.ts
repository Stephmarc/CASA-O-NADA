import { Component, OnInit, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../../core/auth/auth-service';
import { environment } from '../../../environments/environment';

// Panel de inicio: muestra cifras distintas según el rol del usuario.
@Component({
  selector: 'app-panel',
  template: `
    <h1>Panel</h1>
    <p>Resumen de {{ auth.nombreUsuario() }} · {{ rol() }}</p>

    @if (error()) {
      <p role="alert">{{ error() }}</p>
    }

    <section class="tarjetas">
      @for (c of cifras(); track c.titulo) {
        <article class="cifra">
          <span>{{ c.titulo }}</span>
          <strong>{{ c.valor }}</strong>
        </article>
      }
    </section>

    <section class="panel">
      <h2>{{ tituloGrafico() }}</h2>
      @for (b of barras(); track b.nombre) {
        <div class="barra">
          <span>{{ b.nombre }}</span>
          <div class="barra-fondo"><div class="barra-valor" [style.width.%]="b.porcentaje"></div></div>
          <b>{{ b.cantidad }}</b>
        </div>
      } @empty {
        <p>Todavía no hay datos para mostrar.</p>
      }
    </section>

    <section class="panel">
      <h2>{{ tituloTabla() }}</h2>
      <table>
        <thead>
          <tr>
            @for (c of columnas(); track c) {
              <th>{{ c }}</th>
            }
          </tr>
        </thead>
        <tbody>
          @for (fila of filas(); track $index) {
            <tr>
              @for (celda of fila; track $index) {
                <td>{{ celda }}</td>
              }
            </tr>
          } @empty {
            <tr><td [attr.colspan]="columnas().length">Sin registros.</td></tr>
          }
        </tbody>
      </table>
    </section>
  `,
})
export class Panel implements OnInit {
  protected auth = inject(AuthService);
  private http = inject(HttpClient);
  private api = environment.apiBaseUrl + '/api/v1';

  protected error = signal('');
  protected cifras = signal<{ titulo: string; valor: string | number }[]>([]);
  protected barras = signal<{ nombre: string; cantidad: number; porcentaje: number }[]>([]);
  protected columnas = signal<string[]>([]);
  protected filas = signal<(string | number)[][]>([]);
  protected tituloGrafico = signal('');
  protected tituloTabla = signal('');

  rol(): string {
    if (this.auth.tieneRol('ADMIN')) return 'Administrador';
    if (this.auth.tieneRol('VENDEDOR')) return 'Vendedor';
    return 'Cliente';
  }

  ngOnInit() {
    if (this.auth.tieneRol('ADMIN')) this.cargarAdmin();
    else if (this.auth.tieneRol('VENDEDOR')) this.cargarVendedor();
    else this.cargarCliente();
  }

  private cargarAdmin() {
    this.http.get<any[]>(this.api + '/propiedades').subscribe((todas) => {
      const props = todas.filter((p) => p.activo);
      this.http.get<any[]>(this.api + '/pagos').subscribe({
        next: (pagos) => {
          const aprobados = pagos.filter((p) => p.estado === 'VALIDADO');
          const ingresos = aprobados.reduce((suma, p) => suma + Number(p.monto), 0);
          this.cifras.set([
            { titulo: 'Propiedades', valor: props.length },
            { titulo: 'Disponibles', valor: props.filter((p) => p.stock === 1).length },
            { titulo: 'Pagos registrados', valor: pagos.length },
            { titulo: 'Ingresos (simulados)', valor: 'S/ ' + ingresos.toFixed(2) },
          ]);
          this.tituloGrafico.set('Pagos por estado');
          this.barras.set(this.contar(pagos.map((p) => p.estado)));
          this.tituloTabla.set('Últimos pagos');
          this.columnas.set(['Pago', 'Reserva', 'Monto', 'Método', 'Estado']);
          this.filas.set(
            pagos.slice(-8).reverse().map((p) => [p.id, p.reservaId, 'S/ ' + p.monto, p.metodoPago, p.estado]),
          );
        },
        error: () => this.error.set('No se pudieron cargar los pagos.'),
      });
    });
  }

  private cargarVendedor() {
    this.http.get<any[]>(this.api + '/propiedades').subscribe({
      next: (props) => {
        const mias = props.filter((p) => p.activo && p.idVendedor === this.auth.idCliente());
        this.cifras.set([
          { titulo: 'Mis propiedades', valor: mias.length },
          { titulo: 'Disponibles', valor: mias.filter((p) => p.stock === 1).length },
          { titulo: 'Reservadas', valor: mias.filter((p) => p.stock !== 1).length },
        ]);
        this.tituloGrafico.set('Mis propiedades por ciudad');
        this.barras.set(this.contar(mias.map((p) => p.ciudad)));
        this.tituloTabla.set('Mis propiedades');
        this.columnas.set(['Nombre', 'Ciudad', 'Reserva (S/)', 'Estado']);
        this.filas.set(mias.map((p) => [p.nombre, p.ciudad, p.precio, p.stock === 1 ? 'Disponible' : 'Reservada']));
      },
      error: () => this.error.set('No se pudieron cargar las propiedades.'),
    });
  }

  private cargarCliente() {
    this.http.get<any[]>(this.api + '/reservas').subscribe({
      next: (reservas) => {
        const pagadas = reservas.filter((r) => r.estado === 'PAGADA');
        const total = pagadas.reduce((suma, r) => suma + Number(r.total), 0);
        this.cifras.set([
          { titulo: 'Mis reservas', valor: reservas.length },
          { titulo: 'Pagadas', valor: pagadas.length },
          { titulo: 'Canceladas', valor: reservas.filter((r) => r.estado === 'CANCELADA').length },
          { titulo: 'Total pagado', valor: 'S/ ' + total.toFixed(2) },
        ]);
        this.tituloGrafico.set('Mis reservas por estado');
        this.barras.set(this.contar(reservas.map((r) => r.estado)));
        this.tituloTabla.set('Mis últimas reservas');
        this.columnas.set(['Reserva', 'Fecha', 'Total', 'Estado']);
        this.filas.set(
          reservas.slice(-8).reverse().map((r) => [
            r.id,
            String(r.fechaCreacion).slice(0, 10),
            r.total ? 'S/ ' + r.total : '-',
            r.estado,
          ]),
        );
      },
      error: () => this.error.set('No se pudieron cargar las reservas.'),
    });
  }

  // Cuenta cuántas veces aparece cada valor y calcula el ancho de su barra
  private contar(valores: string[]) {
    const conteo = new Map<string, number>();
    valores.forEach((v) => conteo.set(v, (conteo.get(v) ?? 0) + 1));
    const mayor = Math.max(1, ...conteo.values());
    return [...conteo].map(([nombre, cantidad]) => ({ nombre, cantidad, porcentaje: (cantidad / mayor) * 100 }));
  }
}
