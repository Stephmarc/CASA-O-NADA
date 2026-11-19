package pe.edu.upeu.propiedad.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tipos_propiedad")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TipoPropiedad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "descripcion", length = 255)
    private String descripcion;
}