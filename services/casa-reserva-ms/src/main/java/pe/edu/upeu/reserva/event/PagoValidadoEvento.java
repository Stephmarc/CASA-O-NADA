package pe.edu.upeu.reserva.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagoValidadoEvento {

    private String tipoEvento;
    private Long reservaId;
    private BigDecimal monto;
    private String estado;
    private String origen;
    private Long timestamp;
}
