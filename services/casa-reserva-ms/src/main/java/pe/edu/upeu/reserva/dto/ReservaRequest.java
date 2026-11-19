package pe.edu.upeu.reserva.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservaRequest {

    @NotBlank
    private String metodoPago;

    @jakarta.validation.constraints.Size(min=1, max=1)
    @NotEmpty
    @Valid
    private List<DetalleReservaRequest> detalles;
}
