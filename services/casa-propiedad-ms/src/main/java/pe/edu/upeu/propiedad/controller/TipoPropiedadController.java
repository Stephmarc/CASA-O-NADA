package pe.edu.upeu.propiedad.controller;

import pe.edu.upeu.propiedad.dto.TipoPropiedadRequest;
import pe.edu.upeu.propiedad.dto.TipoPropiedadResponse;
import pe.edu.upeu.propiedad.service.TipoPropiedadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/tipos-propiedad")
@RequiredArgsConstructor
public class TipoPropiedadController {

    private final TipoPropiedadService tipoPropiedadService;

    @GetMapping
    public List<TipoPropiedadResponse> listar() {
        log.info("Listando categorías");
        return tipoPropiedadService.listar();
    }

    @GetMapping("/{id}")
    public TipoPropiedadResponse obtener(@PathVariable Long id) {
        return tipoPropiedadService.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TipoPropiedadResponse crear(@Valid @RequestBody TipoPropiedadRequest request) {
        return tipoPropiedadService.crear(request);
    }

    @PutMapping("/{id}")
    public TipoPropiedadResponse actualizar(@PathVariable Long id, @Valid @RequestBody TipoPropiedadRequest request) {
        return tipoPropiedadService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        tipoPropiedadService.eliminar(id);
    }
}
