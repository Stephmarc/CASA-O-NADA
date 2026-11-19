package pe.edu.upeu.cliente;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
@RestController @RequestMapping("/api/v1/clientes") @RequiredArgsConstructor
public class ClienteController {
 private final ClienteRepository repo;
 @GetMapping("/me") public Cliente me(@AuthenticationPrincipal Jwt jwt) {
  return repo.findByUsuarioId(jwt.getSubject()).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Perfil no registrado"));
 }
 @PutMapping("/me") @org.springframework.transaction.annotation.Transactional
 public Cliente guardar(@AuthenticationPrincipal Jwt jwt,@Valid @RequestBody ClienteRequest r) {
  if ((r.tipoDocumento().equals("DNI") && r.documento().length()!=8) || (r.tipoDocumento().equals("RUC") && r.documento().length()!=11))
   throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Documento incompatible con el tipo");
  Cliente c=repo.findByUsuarioId(jwt.getSubject()).orElseGet(Cliente::new);
  c.setUsuarioId(jwt.getSubject());c.setTipoDocumento(r.tipoDocumento());c.setDocumento(r.documento());
  c.setNombre(r.nombre());c.setCorreo(r.correo());c.setTelefono(r.telefono());c.setDireccion(r.direccion());return repo.save(c);
 }
 @DeleteMapping("/me") @ResponseStatus(HttpStatus.NO_CONTENT)
 public void eliminar(@AuthenticationPrincipal Jwt jwt) {repo.delete(me(jwt));}
 @GetMapping @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
 public java.util.List<Cliente> listar(){return repo.findAll();}
}
