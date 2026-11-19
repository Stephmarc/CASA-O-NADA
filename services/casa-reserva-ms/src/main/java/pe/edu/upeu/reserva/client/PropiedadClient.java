package pe.edu.upeu.reserva.client;

import pe.edu.upeu.reserva.dto.PropiedadDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "casa-propiedad-ms")
public interface PropiedadClient {

    @GetMapping("/api/v1/propiedades/{id}")
    PropiedadDto findById(@PathVariable("id") Long id);

    @PatchMapping("/api/v1/propiedades/{id}/descontar-stock")
    void descontarStock(@PathVariable("id") Long id, @RequestParam("cantidad") Integer cantidad);

    @PatchMapping("/api/v1/propiedades/{id}/restaurar-stock")
    void restaurarStock(@PathVariable("id") Long id, @RequestParam("cantidad") Integer cantidad);
        
}
