import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth-guard';
import { rolGuard } from './core/guards/rol-guard';

export const routes: Routes = [
  {
    path: 'auth/callback',
    loadComponent: () => import('./core/auth/auth-callback').then((m) => m.AuthCallback),
  },
  {
    path: '',
    loadComponent: () => import('./core/layout/layout').then((m) => m.Layout),
    children: [
      {
        path: 'mi-perfil',
        canActivate: [authGuard, rolGuard('CLIENTE')],
        loadComponent: () => import('./features/clientes/perfil').then((m) => m.Perfil),
      },
      {
        path: '',
        loadComponent: () => import('./features/propiedad/propiedades').then((m) => m.Propiedades),
      },
      {
        path: 'tipos-propiedad',
        canActivate: [authGuard, rolGuard('ADMIN')],
        loadComponent: () =>
          import('./features/propiedad/tipoPropiedad-list').then((m) => m.TipoPropiedadList),
      },
      {
        path: 'tipos-propiedad/nueva',
        canActivate: [authGuard, rolGuard('ADMIN')],
        loadComponent: () =>
          import('./features/propiedad/tipoPropiedad-form').then((m) => m.TipoPropiedadForm),
      },
      {
        path: 'tipos-propiedad/:id/editar',
        canActivate: [authGuard, rolGuard('ADMIN')],
        loadComponent: () =>
          import('./features/propiedad/tipoPropiedad-form').then((m) => m.TipoPropiedadForm),
      },
      {
        path: 'reservas/nueva',
        canActivate: [authGuard, rolGuard('CLIENTE')],
        loadComponent: () =>
          import('./features/reservas/reserva-crear').then((m) => m.ReservaCrear),
      },
      {
        path: 'mis-pedidos',
        canActivate: [authGuard, rolGuard('CLIENTE')],
        loadComponent: () => import('./features/reservas/mis-pedidos').then((m) => m.MisPedidos),
      },
      {
        path: 'pagos',
        canActivate: [authGuard, rolGuard('ADMIN')],
        loadComponent: () => import('./features/reservas/pago-list').then((m) => m.PagoList),
      },
    ],
  },
];
