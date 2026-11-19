package pe.edu.upeu.propiedad.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "propiedades")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Propiedad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "descripcion", length = 255)
    private String descripcion;

    @Column(name = "precio", nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    @Column(name = "activo", nullable = false)
    private Boolean activo;

    @Column(name = "stock", nullable = false)
    private Integer stock;

    @Column(nullable = false, length = 200)
    private String direccion;
    @Column(nullable = false, length = 80)
    private String ciudad;
    @Column(nullable = false)
    private Integer areaM2;
    @Column(nullable = false)
    private Integer dormitorios;

    @Version
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_propiedad", nullable = false)
    private TipoPropiedad tipoPropiedad;
}