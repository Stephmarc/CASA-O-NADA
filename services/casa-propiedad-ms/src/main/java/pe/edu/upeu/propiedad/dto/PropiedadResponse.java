package pe.edu.upeu.propiedad.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropiedadResponse {
    private Long id;
    private String direccion;
    private String ciudad;
    private Integer areaM2;
    private Integer dormitorios;
    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private Boolean activo;
    private Integer stock;
    private String idVendedor;
    private TipoPropiedadResponse tipoPropiedad;
}