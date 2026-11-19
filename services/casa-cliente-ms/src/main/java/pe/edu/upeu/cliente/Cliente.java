package pe.edu.upeu.cliente;
import jakarta.persistence.*;
import lombok.*;
@Entity @Table(name="clientes") @Getter @Setter @NoArgsConstructor
public class Cliente {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false,unique=true,length=100) private String usuarioId;
 @Column(nullable=false,length=3) private String tipoDocumento;
 @Column(nullable=false,unique=true,length=11) private String documento;
 @Column(nullable=false,length=180) private String nombre;
 @Column(nullable=false,length=180) private String correo;
 @Column(length=30) private String telefono;
 @Column(length=200) private String direccion;
}
