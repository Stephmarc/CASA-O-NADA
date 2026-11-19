import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { PropiedadService } from '../propiedad/propiedad-service';
import { Propiedad } from '../propiedad/propiedad.model';
import { ReservaService } from './reserva-service';
import { ReservaResponse } from './reserva.model';

interface ItemSolicitud {
  propiedad: Propiedad;
  cantidad: number;
}

@Component({
  selector: 'app-reserva-crear',
  imports: [RouterLink, FormsModule],
  templateUrl: './reserva-crear.html',
})
export class ReservaCrear implements OnInit {
  private readonly propiedadService = inject(PropiedadService);
  private readonly reservaService = inject(ReservaService);

  protected readonly propiedades = signal<Propiedad[]>([]);
  protected readonly solicitud = signal<ItemSolicitud[]>([]);
  protected readonly cargando = signal(true);
  protected readonly enviando = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly resultado = signal<ReservaResponse | null>(null);
  protected metodoPago = 'YAPE_PLIN';

  protected readonly total = computed(() =>
    this.solicitud().reduce((suma, item) => suma + item.propiedad.precio * item.cantidad, 0),
  );

  ngOnInit(): void {
    this.propiedadService.listar().subscribe({
      next: (data) => {
        this.propiedades.set(data.filter((p) => p.activo));
        this.cargando.set(false);
      },
      error: () => {
        this.error.set('No se pudo cargar el catálogo de propiedades.');
        this.cargando.set(false);
      },
    });
  }

  agregar(propiedad: Propiedad, cantidad: number): void {
    if (propiedad.stock !== 1) return;
    this.solicitud.set([{ propiedad, cantidad: 1 }]);
  }

  quitar(idPropiedad: number): void {
    this.solicitud.update((lista) => lista.filter((item) => item.propiedad.id !== idPropiedad));
  }

  confirmar(): void {
    if (this.solicitud().length === 0) return;
    this.enviando.set(true);
    this.error.set(null);
    this.reservaService
      .crear({
        metodoPago: this.metodoPago,
        detalles: this.solicitud().map((item) => ({
          idPropiedad: item.propiedad.id,
          cantidad: item.cantidad,
        })),
      })
      .subscribe({
        next: (reserva) => {
          this.resultado.set(reserva);
          this.solicitud.set([]);
          this.enviando.set(false);
        },
        error: () => {
          this.error.set('No se pudo crear la reserva.');
          this.enviando.set(false);
        },
      });
  }
}
