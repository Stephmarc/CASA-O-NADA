package pe.edu.upeu.reserva.repository;

import pe.edu.upeu.reserva.entity.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("SELECT r FROM Reserva r WHERE r.id = :id")
    java.util.Optional<Reserva> bloquear(@org.springframework.data.repository.query.Param("id") Long id);

    List<Reserva> findByIdCliente(String idCliente);
}
