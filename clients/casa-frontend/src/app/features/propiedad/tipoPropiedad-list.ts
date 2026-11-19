import { Component, OnInit, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { TipoPropiedadService } from './tipoPropiedad-service';
import { TipoPropiedad } from './tipoPropiedad.model';

@Component({
  selector: 'app-tipoPropiedad-list',
  imports: [RouterLink],
  templateUrl: './tipoPropiedad-list.html',
})
export class TipoPropiedadList implements OnInit {
  private readonly tipoPropiedadService = inject(TipoPropiedadService);
  protected readonly tiposPropiedad = signal<TipoPropiedad[]>([]);
  protected readonly cargando = signal(true);
  protected readonly error = signal<string | null>(null);
  protected readonly errorAccion = signal<string | null>(null);

  ngOnInit(): void {
    this.cargar();
  }

  private cargar(): void {
    this.cargando.set(true);
    this.tipoPropiedadService.listar().subscribe({
      next: (data) => {
        this.tiposPropiedad.set(data);
        this.cargando.set(false);
      },
      error: () => {
        this.error.set('No se pudo cargar la lista de tipos-propiedad.');
        this.cargando.set(false);
      },
    });
  }

  eliminar(id: number): void {
    if (!confirm(`¿Seguro que quieres eliminar la categoría ${id}?`)) {
      return;
    }
    this.errorAccion.set(null);
    this.tipoPropiedadService.eliminar(id).subscribe({
      next: () => this.cargar(),
      error: (err: HttpErrorResponse) => {
        if (err.status === 500) {
          this.errorAccion.set('No se puede eliminar: la categoría tiene propiedades asociados.');
        } else if (err.status === 403) {
          this.errorAccion.set('No tienes permiso para esta acción.');
        } else {
          this.errorAccion.set('No se pudo eliminar la categoría.');
        }
      },
    });
  }
}
