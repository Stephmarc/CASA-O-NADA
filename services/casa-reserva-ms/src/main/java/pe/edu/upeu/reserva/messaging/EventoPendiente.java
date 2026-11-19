package pe.edu.upeu.reserva.messaging;
@jakarta.persistence.Entity @jakarta.persistence.Table(name="eventos_pendientes")
@lombok.Getter @lombok.Setter @lombok.NoArgsConstructor
public class EventoPendiente {
 @jakarta.persistence.Id @jakarta.persistence.GeneratedValue(strategy=jakarta.persistence.GenerationType.IDENTITY) private Long id;
 @jakarta.persistence.Embedded private pe.edu.upeu.reserva.event.ReservaCreadaEvento evento;
}
