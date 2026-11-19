package pe.edu.upeu.cliente;
@org.springframework.web.bind.annotation.RestControllerAdvice
public class Errores {
 @org.springframework.web.bind.annotation.ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
 public org.springframework.http.ResponseEntity<?> duplicado(){return org.springframework.http.ResponseEntity.status(409).body(java.util.Map.of("mensaje","El documento ya está registrado"));}
}
