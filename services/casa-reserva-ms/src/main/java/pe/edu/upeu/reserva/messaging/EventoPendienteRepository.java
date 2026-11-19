package pe.edu.upeu.reserva.messaging;
public interface EventoPendienteRepository extends org.springframework.data.jpa.repository.JpaRepository<EventoPendiente,Long> {
 @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
 @org.springframework.data.jpa.repository.Query("select e from EventoPendiente e order by e.id")
 java.util.List<EventoPendiente> pendientes(org.springframework.data.domain.Pageable limite);
}
