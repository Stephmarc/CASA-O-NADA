package pe.edu.upeu.reserva.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import pe.edu.upeu.reserva.client.PropiedadClient;
import pe.edu.upeu.reserva.dto.PropiedadDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PropiedadConsultaService {

    private final PropiedadClient propiedadClient;

    @CircuitBreaker(name = "propiedad", fallbackMethod = "fallbackPropiedad")
    public PropiedadDto consultarPropiedad(Long idPropiedad) {
        return propiedadClient.findById(idPropiedad);
    }

    public PropiedadDto fallbackPropiedad(Long idPropiedad, Throwable ex) {
        log.warn("[PROPIEDAD] Fallback activado para idPropiedad {}. Motivo: {}", idPropiedad, ex.getMessage());
        return null;
    }

    @CircuitBreaker(name = "propiedad", fallbackMethod = "fallbackDescontarStock")
    public boolean descontarStock(Long idPropiedad, Integer cantidad) {
        propiedadClient.descontarStock(idPropiedad, cantidad);
        return true;
    }

    public boolean fallbackDescontarStock(Long idPropiedad, Integer cantidad, Throwable ex) {
        log.warn("[PROPIEDAD] No se pudo descontar stock de {}. Motivo: {}", idPropiedad, ex.getMessage());
        return false;
    }
    
    @CircuitBreaker(name = "propiedad", fallbackMethod = "fallbackRestaurarStock")
    public void restaurarStock(Long idPropiedad, Integer cantidad) {
        propiedadClient.restaurarStock(idPropiedad, cantidad);
    }

    public void fallbackRestaurarStock(Long idPropiedad, Integer cantidad, Throwable ex) {
        log.error("[PROPIEDAD] No se pudo restaurar stock de {} al compensar. Motivo: {} — requiere correccion manual",
                idPropiedad, ex.getMessage());
        throw new IllegalStateException("Compensación pendiente: propiedad no disponible", ex);
    }

}
