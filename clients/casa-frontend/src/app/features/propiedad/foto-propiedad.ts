import { Component, Input, signal } from '@angular/core';

const FOTOS = {
  casa: {
    archivo: 'casa-referencia.jpg',
    descripcion: 'Casa con jardín',
    autor: 'jirayu koontholjinda',
    fuente: 'https://unsplash.com/photos/UnUFaL8alf4',
  },
  departamento: {
    archivo: 'departamento-referencia.jpg',
    descripcion: 'Edificio de departamentos',
    autor: 'Yansi Keim',
    fuente: 'https://unsplash.com/photos/_4U19TDp1bw',
  },
  terreno: {
    archivo: 'terreno-referencia.jpg',
    descripcion: 'Terreno abierto con vegetación',
    autor: 'Jim McCormick',
    fuente: 'https://unsplash.com/photos/brZ9zzbcqKw',
  },
};

@Component({
  selector: 'app-foto-propiedad',
  template: `
    <figure>
      @if (!fallo()) {
        <img
          [src]="'/images/' + foto.archivo"
          [alt]="foto.descripcion + ': fotografía de referencia, no del inmueble anunciado'"
          width="960"
          height="640"
          loading="lazy"
          decoding="async"
          (error)="fallo.set(true)"
        />
      } @else {
        <div class="sin-foto">Fotografía no disponible</div>
      }
      <figcaption>
        Foto de referencia ·
        <a [href]="foto.fuente" target="_blank" rel="noopener noreferrer">{{ foto.autor }}</a>
      </figcaption>
    </figure>
  `,
  styles: `
    :host {
      display: block;
      margin-bottom: 20px;
    }
    figure {
      margin: 0;
    }
    img,
    .sin-foto {
      display: block;
      width: 100%;
      height: auto;
      aspect-ratio: 3 / 2;
      object-fit: cover;
      border-radius: 8px;
      background: #e1e9dc;
    }
    .sin-foto {
      display: grid;
      place-items: center;
      color: #526562;
    }
    figcaption {
      margin-top: 7px;
      font-size: 0.7rem;
      line-height: 1.5;
      color: #526562;
    }
  `,
})
export class FotoPropiedad {
  @Input() tipo = '';
  protected readonly fallo = signal(false);

  protected get foto() {
    const tipo = this.tipo.toLocaleLowerCase('es');
    if (tipo.includes('terreno')) return FOTOS.terreno;
    if (tipo.includes('departamento')) return FOTOS.departamento;
    return FOTOS.casa;
  }
}
