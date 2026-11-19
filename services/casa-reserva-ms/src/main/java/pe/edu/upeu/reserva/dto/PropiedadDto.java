package pe.edu.upeu.reserva.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropiedadDto {
    private Long id;
    private String nombre;
    private BigDecimal precio;
    private Boolean activo;
}
