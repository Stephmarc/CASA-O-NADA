package pe.edu.upeu.cliente;
import jakarta.validation.constraints.*;
public record ClienteRequest(
 @NotBlank @Pattern(regexp="DNI|RUC") String tipoDocumento,
 @NotBlank @Pattern(regexp="[0-9]{8}|[0-9]{11}") String documento,
 @NotBlank @Size(max=180) String nombre,
 @NotBlank @Email @Size(max=180) String correo,
 @Size(max=30) String telefono, @Size(max=200) String direccion) {}
