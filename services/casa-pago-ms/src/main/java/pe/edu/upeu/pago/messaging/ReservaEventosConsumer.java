package pe.edu.upeu.pago.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import pe.edu.upeu.pago.event.ReservaCreadaEvento;
import pe.edu.upeu.pago.service.PagoService;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservaEventosConsumer {

    private static final String RESERVA_CREADA = "reserva.creada";

    private final PagoService pagoService;

    @KafkaListener(topics = "${app.kafka.topic.reservas}")
    public void alRecibirReserva(ReservaCreadaEvento evento) {
        if (!RESERVA_CREADA.equals(evento.getTipoEvento())) {
            log.warn("component=consumer eventType={} status=ignored", evento.getTipoEvento());
            return;
        }
        log.info("component=consumer eventType={} reservaId={} status=consumed",
                evento.getTipoEvento(), evento.getReservaId());
        pagoService.procesar(evento);
    }
}
