package pe.edu.upeu.reserva.service;

import pe.edu.upeu.reserva.dto.*;
import pe.edu.upeu.reserva.event.ReservaCreadaEvento;
import pe.edu.upeu.reserva.exception.ResourceNotFoundException;
import pe.edu.upeu.reserva.messaging.ReservaEventosProducer;
import pe.edu.upeu.reserva.entity.EstadoReserva;
import pe.edu.upeu.reserva.entity.Reserva;
import pe.edu.upeu.reserva.entity.ReservaDetalle;
import pe.edu.upeu.reserva.repository.ReservaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservaServiceImpl implements ReservaService {

    private final ReservaRepository reservaRepository;
    private final PropiedadConsultaService propiedadConsultaService;
    private final ReservaEventosProducer producer;

    @Override
    @Transactional
    public ReservaResponse crear(ReservaRequest request, String idCliente) {
        Reserva reserva = Reserva.builder()
                .idCliente(idCliente)
                .metodoPago(request.getMetodoPago())
                .build();

        List<ReservaDetalle> detalles = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        boolean validacionCompleta = true;

        for (DetalleReservaRequest item : request.getDetalles()) {
            PropiedadDto propiedad = propiedadConsultaService.consultarPropiedad(item.getIdPropiedad());
            boolean stockReservado = propiedad != null
                    && propiedadConsultaService.descontarStock(item.getIdPropiedad(), item.getCantidad());

            if (!stockReservado) {
                validacionCompleta = false;
                detalles.add(ReservaDetalle.builder()
                        .reserva(reserva)
                        .idPropiedad(item.getIdPropiedad())
                        .nombrePropiedad(null)
                        .cantidad(item.getCantidad())
                        .precioUnitario(null)
                        .build());
                continue;
            }

            BigDecimal subtotal = propiedad.getPrecio()
                    .multiply(BigDecimal.valueOf(item.getCantidad()));
            total = total.add(subtotal);

            detalles.add(ReservaDetalle.builder()
                    .reserva(reserva)
                    .idPropiedad(item.getIdPropiedad())
                    .nombrePropiedad(propiedad.getNombre())
                    .cantidad(item.getCantidad())
                    .precioUnitario(propiedad.getPrecio())
                    .build());
        }

        reserva.setDetalles(detalles);
        reserva.setTotal(validacionCompleta ? total : null);
        reserva.setEstado(validacionCompleta ? EstadoReserva.PENDIENTE_PAGO : EstadoReserva.CARRITO);

        Reserva guardada = reservaRepository.save(reserva);
        log.info("component=service reservaId={} estado={} total={} status=created",
                guardada.getId(), guardada.getEstado(), guardada.getTotal());

        if (guardada.getEstado() == EstadoReserva.PENDIENTE_PAGO) {
            producer.publicarTrasCommit(ReservaCreadaEvento.builder()
                    .tipoEvento("reserva.creada")
                    .reservaId(guardada.getId())
                    .idCliente(guardada.getIdCliente())
                    .total(guardada.getTotal())
                    .metodoPago(guardada.getMetodoPago())
                    .origen("casa-reserva-ms")
                    .timestamp(Instant.now().toEpochMilli())
                    .build());
        }
        return toResponse(guardada);
    }

    @Override
    @Transactional
    public void marcarPagada(Long reservaId) {
        Reserva reserva = reservaRepository.bloquear(reservaId).orElse(null);
        if (reserva == null || reserva.getEstado() != EstadoReserva.PENDIENTE_PAGO) {
            log.warn("component=processor reservaId={} status=ignored motivo=\"la reserva no existe o no esta pendiente de pago\"", reservaId);
            return;
        }
        reserva.setEstado(EstadoReserva.PAGADA);
        log.info("component=processor reservaId={} estado={} status=processed", reservaId, reserva.getEstado());
    }

    @Override
    @Transactional(readOnly = true)
    public ReservaResponse findById(Long id, String idCliente) {
        Reserva reserva = reservaRepository.findById(id)
                .filter(o -> o.getIdCliente().equals(idCliente))
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada: " + id));
        return toResponse(reserva);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservaResponse> listar(String idCliente) {
        return reservaRepository.findByIdCliente(idCliente).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private ReservaResponse toResponse(Reserva reserva) {
        List<DetalleReservaResponse> detalles = reserva.getDetalles().stream()
                .map(d -> DetalleReservaResponse.builder()
                        .idPropiedad(d.getIdPropiedad())
                        .nombrePropiedad(d.getNombrePropiedad())
                        .cantidad(d.getCantidad())
                        .precioUnitario(d.getPrecioUnitario())
                        .subtotal(d.getPrecioUnitario() == null
                                ? null
                                : d.getPrecioUnitario().multiply(BigDecimal.valueOf(d.getCantidad())))
                        .build())
                .collect(Collectors.toList());

        return ReservaResponse.builder()
                .id(reserva.getId())
                .idCliente(reserva.getIdCliente())
                .fechaCreacion(reserva.getFechaCreacion())
                .estado(reserva.getEstado().name())
                .total(reserva.getTotal())
                .detalles(detalles)
                .build();
    }

    @Override
    @Transactional
    public void compensar(Long reservaId) {
        Reserva reserva = reservaRepository.bloquear(reservaId).orElse(null);
        if (reserva == null || reserva.getEstado() != EstadoReserva.PENDIENTE_PAGO) {
            log.warn("component=processor reservaId={} status=ignored motivo=\"la reserva no existe o ya no esta pendiente de pago\"", reservaId);
            return;
        }
        for (ReservaDetalle detalle : reserva.getDetalles()) {
            propiedadConsultaService.restaurarStock(detalle.getIdPropiedad(), detalle.getCantidad());
        }
        reserva.setEstado(EstadoReserva.CANCELADA);
        log.info("component=processor reservaId={} estado={} status=compensated", reservaId, reserva.getEstado());
    }

}
