package pe.edu.upeu.reserva.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetalleReservaRequest {

    @NotNull
    private Long idPropiedad;

    @NotNull
    @Positive
    @jakarta.validation.constraints.Max(1)
    private Integer cantidad;
}
