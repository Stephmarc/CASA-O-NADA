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
@jakarta.persistence.Embeddable
public class ReservaCreadaEvento {

    private String tipoEvento;
    private Long reservaId;
    private String idCliente;
    private BigDecimal total;
    private String metodoPago;
    private String origen;
    private Long timestamp;
}
