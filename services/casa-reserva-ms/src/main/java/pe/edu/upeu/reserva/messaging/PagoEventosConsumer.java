package pe.edu.upeu.reserva.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import pe.edu.upeu.reserva.event.PagoValidadoEvento;
import pe.edu.upeu.reserva.service.ReservaService;

@Slf4j
@Component
@RequiredArgsConstructor
public class PagoEventosConsumer {

    private static final String PAGO_VALIDADO = "pago.validado";
    private static final String PAGO_FALLIDO = "pago.fallido";

    private final ReservaService reservaService;

    @KafkaListener(topics = "${app.kafka.topic.pagos}")
    public void alRecibirPago(PagoValidadoEvento evento) {
        switch (evento.getTipoEvento()) {
            case PAGO_VALIDADO -> {
                log.info("component=consumer eventType={} reservaId={} status=consumed",
                        evento.getTipoEvento(), evento.getReservaId());
                reservaService.marcarPagada(evento.getReservaId());
            }
            case PAGO_FALLIDO -> {
                log.info("component=consumer eventType={} reservaId={} status=consumed",
                        evento.getTipoEvento(), evento.getReservaId());
                reservaService.compensar(evento.getReservaId());
            }
            default -> log.warn("component=consumer eventType={} status=ignored", evento.getTipoEvento());
        }
    }
}
