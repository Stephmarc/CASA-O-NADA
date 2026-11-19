package pe.edu.upeu.reserva.controller;

import pe.edu.upeu.reserva.dto.ReservaRequest;
import pe.edu.upeu.reserva.dto.ReservaResponse;
import pe.edu.upeu.reserva.service.ReservaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/reservas")
@RequiredArgsConstructor
public class ReservaController {

    private final ReservaService reservaService;

@PostMapping
@ResponseStatus(HttpStatus.CREATED)
public ReservaResponse crear(@Valid @RequestBody ReservaRequest request,
                            @AuthenticationPrincipal Jwt jwt) {
    String idCliente = jwt.getSubject();
    return reservaService.crear(request, idCliente);
}


    @GetMapping
    public List<ReservaResponse> listar(@AuthenticationPrincipal Jwt jwt) {
        return reservaService.listar(jwt.getSubject());
    }

    @GetMapping("/{id}")
    public ReservaResponse findById(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return reservaService.findById(id, jwt.getSubject());
    }
}
