package pe.edu.upeu.reserva.messaging;
import pe.edu.upeu.reserva.event.ReservaCreadaEvento;
@org.springframework.stereotype.Component @lombok.RequiredArgsConstructor @lombok.extern.slf4j.Slf4j
@org.springframework.scheduling.annotation.EnableScheduling
public class ReservaEventosProducer {
 private final EventoPendienteRepository repo;
 private final org.springframework.kafka.core.KafkaTemplate<String,ReservaCreadaEvento> kafka;
 @org.springframework.beans.factory.annotation.Value("${app.kafka.topic.reservas}") private String topic;
 public void publicarTrasCommit(ReservaCreadaEvento evento) {
  EventoPendiente pendiente=new EventoPendiente(); pendiente.setEvento(evento); repo.save(pendiente);
 }
 @org.springframework.scheduling.annotation.Scheduled(fixedDelay=2000)
 @org.springframework.transaction.annotation.Transactional
 public void despachar() {
  for(EventoPendiente p:repo.pendientes(org.springframework.data.domain.PageRequest.of(0,20))) {
   try {kafka.send(topic,String.valueOf(p.getEvento().getReservaId()),p.getEvento()).get(10,java.util.concurrent.TimeUnit.SECONDS);repo.delete(p);}
   catch(Exception ex) {log.warn("Evento pendiente de reintento reservaId={}",p.getEvento().getReservaId());break;}
  }
 }
}
