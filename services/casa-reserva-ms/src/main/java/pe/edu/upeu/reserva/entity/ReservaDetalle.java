package pe.edu.upeu.reserva.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "reserva_detalles")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservaDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_reserva", nullable = false)
    private Reserva reserva;

    @Column(name = "id_propiedad", nullable = false)
    private Long idPropiedad;

    @Column(name = "nombre_propiedad", length = 150)
    private String nombrePropiedad;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(name = "precio_unitario")
    private BigDecimal precioUnitario;
}
