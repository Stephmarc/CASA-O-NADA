package pe.edu.upeu.pago.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.pago.dto.PagoResponse;
import pe.edu.upeu.pago.entity.EstadoPago;
import pe.edu.upeu.pago.entity.Pago;
import pe.edu.upeu.pago.event.ReservaCreadaEvento;
import pe.edu.upeu.pago.event.PagoValidadoEvento;
import pe.edu.upeu.pago.exception.ResourceNotFoundException;
import pe.edu.upeu.pago.mapper.PagoMapper;
import pe.edu.upeu.pago.messaging.PagoEventosProducer;
import pe.edu.upeu.pago.repository.PagoRepository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;

@Slf4j
@Service
@RequiredArgsConstructor
public class PagoServiceImpl implements PagoService {

    private static final String PAGO_VALIDADO = "pago.validado";

    private final PagoRepository pagoRepository;
    private final PagoEventosProducer producer;
    private final PagoMapper pagoMapper;

    @Value("${spring.application.name}")
    private String nombreServicio;

    private static final String PAGO_FALLIDO = "pago.fallido";
    private static final String METODO_RECHAZADO = "TARJETA_RECHAZADA";

    @Override
    @Transactional
    public void procesar(ReservaCreadaEvento reserva) {
        if (pagoRepository.findByReservaId(reserva.getReservaId()).isPresent()) {
            log.warn("component=processor reservaId={} status=ignored motivo=\"pago ya procesado (evento duplicado)\"",
                    reserva.getReservaId());
            return;
        }

        boolean rechazado = METODO_RECHAZADO.equalsIgnoreCase(reserva.getMetodoPago());
        EstadoPago estadoResultante = rechazado ? EstadoPago.FALLIDO : EstadoPago.VALIDADO;

        Pago pago;
        try {
            pago = pagoRepository.save(Pago.builder()
                    .reservaId(reserva.getReservaId())
                    .idCliente(reserva.getIdCliente())
                    .monto(reserva.getTotal())
                    .metodoPago(reserva.getMetodoPago())
                    .estado(estadoResultante)
                    .fechaPago(LocalDateTime.now())
                    .build());
        } catch (DataIntegrityViolationException ex) {
            log.warn("component=processor reservaId={} status=ignored motivo=\"pago ya procesado (condicion de carrera)\"",
                    reserva.getReservaId());
            return;
        }

        String tipoEventoResultante = rechazado ? PAGO_FALLIDO : PAGO_VALIDADO;
        producer.publicarTrasCommit(PagoValidadoEvento.builder()
                .tipoEvento(tipoEventoResultante)
                .reservaId(pago.getReservaId())
                .monto(pago.getMonto())
                .estado(pago.getEstado().name())
                .origen(nombreServicio)
                .timestamp(Instant.now().toEpochMilli())
                .build());

        log.info("component=processor reservaId={} estado={} status=processed", pago.getReservaId(), pago.getEstado());
    }

    @Override
    public List<PagoResponse> listar() {
        return pagoRepository.findAll().stream()
                .map(pagoMapper::toResponse)
                .toList();
    }

    @Override
    public PagoResponse obtener(Long id, String idCliente) {
        return pagoRepository.findById(id)
                .filter(p -> idCliente.equals(p.getIdCliente()))
                .map(pagoMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Pago no encontrado: " + id));
    }

    @Override
    public Optional<PagoResponse> buscarPorReserva(Long reservaId, String idCliente) {
        return pagoRepository.findByReservaId(reservaId)
                .filter(p -> idCliente.equals(p.getIdCliente()))
                .map(pagoMapper::toResponse);
    }
}
