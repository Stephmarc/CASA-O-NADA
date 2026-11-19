import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { TipoPropiedadService } from './tipoPropiedad-service';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-tipoPropiedad-form',
  imports: [ReactiveFormsModule],
  templateUrl: './tipoPropiedad-form.html',
})
export class TipoPropiedadForm {
  private readonly fb = inject(FormBuilder);
  private readonly tipoPropiedadService = inject(TipoPropiedadService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  protected readonly id = signal<number | null>(null);
  protected readonly cargando = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly form = this.fb.nonNullable.group({
    nombre: ['', [Validators.required, Validators.maxLength(100)]],
    descripcion: ['', [Validators.maxLength(255)]],
  });

  constructor() {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      const id = Number(idParam);
      this.id.set(id);
      this.cargando.set(true);
      this.tipoPropiedadService.obtener(id).subscribe({
        next: (tipoPropiedad) => {
          this.form.patchValue(tipoPropiedad);
          this.cargando.set(false);
        },
        error: () => {
          this.error.set('No se pudo cargar la categoría.');
          this.cargando.set(false);
        },
      });
    }
  }

  guardar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const valor = this.form.getRawValue();
    const id = this.id();
    const peticion = id
      ? this.tipoPropiedadService.actualizar(id, valor)
      : this.tipoPropiedadService.crear(valor);

    peticion.subscribe({
      next: () => this.router.navigate(['/tipos-propiedad']),
      error: (err: HttpErrorResponse) => {
        this.error.set(
          err.status === 403
            ? 'No tienes permiso para esta acción.'
            : 'No se pudo guardar la categoría.',
        );
      },
    });
  }

  cancelar(): void {
    this.router.navigate(['/tipos-propiedad']);
  }

  protected mensajeValidacion(campo: 'nombre' | 'descripcion'): string {
    const control = this.form.controls[campo];

    if (!control.touched) return '';
    if (control.hasError('required')) return 'Este campo es obligatorio.';
    if (control.hasError('maxlength')) {
      return `Máximo ${control.getError('maxlength').requiredLength} caracteres.`;
    }
    return '';
  }
}
