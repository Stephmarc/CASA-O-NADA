package pe.edu.upeu.cliente;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.client.RestClient;
@RestController @RequestMapping("/api/v1/documentos")
public class DocumentoController {
 @Value("${documentos.token:}") private String token;
 @Value("${documentos.url:https://api.apis.net.pe/v2}") private String url;
 @GetMapping("/{tipo}/{numero}") public Object consultar(@PathVariable String tipo,@PathVariable String numero) {
  String endpoint;
  if(tipo.equals("DNI") && numero.matches("[0-9]{8}")) endpoint="/reniec/dni";
  else if(tipo.equals("RUC") && numero.matches("[0-9]{11}")) endpoint="/sunat/ruc";
  else throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Usa DNI de 8 dígitos o RUC de 11");
  if(token.isBlank()) throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"Configura DOCUMENTOS_TOKEN para consultar el proveedor RENIEC/SUNAT");
  try {return RestClient.create(url).get().uri(endpoint+"?numero="+numero).headers(h->h.setBearerAuth(token)).retrieve().body(java.util.Map.class);}
  catch(Exception ex){throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,"El proveedor de documentos no respondió");}
 }
}
