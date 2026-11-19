import { Component, inject, signal, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
@Component({
  selector: 'app-perfil',
  imports: [FormsModule],
  template: `<h1>Mi perfil</h1>
    <p>Tu información de contacto para coordinar la reserva.</p>
    <p role="status">{{ mensaje() }}</p>
    <form (ngSubmit)="guardar()">
      <label
        >Documento<select name="tipo" [(ngModel)]="form.tipoDocumento">
          <option>DNI</option>
          <option>RUC</option>
        </select></label
      ><label>Número<input name="numero" [(ngModel)]="form.documento" required /></label
      ><button type="button" (click)="consultar()">Consultar documento</button
      ><label>Nombre o razón social<input name="nombre" [(ngModel)]="form.nombre" required /></label
      ><label>Correo<input name="correo" type="email" [(ngModel)]="form.correo" required /></label
      ><label>Teléfono<input name="telefono" [(ngModel)]="form.telefono" /></label
      ><label>Dirección<input name="direccion" [(ngModel)]="form.direccion" /></label
      ><button>Guardar perfil</button>
    </form>`,
})
export class Perfil implements OnInit {
  private http = inject(HttpClient);
  private base = environment.apiBaseUrl + '/api/v1';
  protected mensaje = signal('');
  protected form: any = {
    tipoDocumento: 'DNI',
    documento: '',
    nombre: '',
    correo: '',
    telefono: '',
    direccion: '',
  };
  ngOnInit() {
    this.http.get(this.base + '/clientes/me').subscribe({
      next: (x) => {
        this.form = x;
        this.mensaje.set('Perfil cargado.');
      },
      error: () => this.mensaje.set('Completa tu perfil para registrarlo.'),
    });
  }
  guardar() {
    this.http
      .put(this.base + '/clientes/me', this.form)
      .subscribe({
        next: () => this.mensaje.set('Perfil guardado.'),
        error: () =>
          this.mensaje.set('Revisa los datos: el documento debe ser único y tener 8 u 11 dígitos.'),
      });
  }
  consultar() {
    this.http
      .get<any>(this.base + '/documentos/' + this.form.tipoDocumento + '/' + this.form.documento)
      .subscribe({
        next: (x) => {
          this.form.nombre =
            x.razonSocial ||
            [x.nombres, x.apellidoPaterno, x.apellidoMaterno].filter(Boolean).join(' ');
          this.mensaje.set('Datos consultados. Verifícalos antes de guardar.');
        },
        error: () =>
          this.mensaje.set('Consulta no disponible. Puedes ingresar tus datos manualmente.'),
      });
  }
}
