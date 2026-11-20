package pe.edu.upeu.propiedad.controller;

import pe.edu.upeu.propiedad.dto.PropiedadRequest;
import pe.edu.upeu.propiedad.dto.PropiedadResponse;
import pe.edu.upeu.propiedad.service.PropiedadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/propiedades")
@RequiredArgsConstructor
public class PropiedadController {

    private final PropiedadService propiedadService;

    @GetMapping
    public List<PropiedadResponse> listar() {
        return propiedadService.listar();
    }

    @GetMapping("/{id}")
    public PropiedadResponse obtener(@PathVariable Long id) {
        return propiedadService.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PropiedadResponse crear(@Valid @RequestBody PropiedadRequest request, @AuthenticationPrincipal Jwt jwt) {
        return propiedadService.crear(request, jwt.getSubject());
    }

    @PutMapping("/{id}")
    public PropiedadResponse actualizar(@PathVariable Long id, @Valid @RequestBody PropiedadRequest request,
                                        @AuthenticationPrincipal Jwt jwt, Authentication auth) {
        return propiedadService.actualizar(id, request, jwt.getSubject(), esAdmin(auth));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt, Authentication auth) {
        propiedadService.eliminar(id, jwt.getSubject(), esAdmin(auth));
    }

    private boolean esAdmin(Authentication auth) {
        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
    
    @PatchMapping("/{id}/descontar-stock")
    public void descontarStock(@PathVariable Long id, @RequestParam Integer cantidad) {
        propiedadService.descontarStock(id, cantidad);
        log.info("component=controller idPropiedad={} cantidad={} operacion=descontar-stock status=ok", id, cantidad);
    }

    @PatchMapping("/{id}/restaurar-stock")
    public void restaurarStock(@PathVariable Long id, @RequestParam Integer cantidad) {
        propiedadService.restaurarStock(id, cantidad);
        log.info("component=controller idPropiedad={} cantidad={} operacion=restaurar-stock status=ok", id, cantidad);
    }

}