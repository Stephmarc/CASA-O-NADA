package pe.edu.upeu.pago.service;

import pe.edu.upeu.pago.dto.PagoResponse;
import pe.edu.upeu.pago.event.ReservaCreadaEvento;

import java.util.List;
import java.util.Optional;

public interface PagoService {
    void procesar(ReservaCreadaEvento reserva);

    List<PagoResponse> listar();

    PagoResponse obtener(Long id, String idCliente);

    Optional<PagoResponse> buscarPorReserva(Long reservaId, String idCliente);
}
