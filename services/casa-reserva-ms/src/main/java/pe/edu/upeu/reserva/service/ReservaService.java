package pe.edu.upeu.reserva.service;

import pe.edu.upeu.reserva.dto.ReservaRequest;
import pe.edu.upeu.reserva.dto.ReservaResponse;
import java.util.List;

public interface ReservaService {
    ReservaResponse crear(ReservaRequest request, String idCliente);
    void marcarPagada(Long reservaId);
    ReservaResponse findById(Long id, String idCliente);
    List<ReservaResponse> listar(String idCliente);

    void compensar(Long reservaId);    
}
