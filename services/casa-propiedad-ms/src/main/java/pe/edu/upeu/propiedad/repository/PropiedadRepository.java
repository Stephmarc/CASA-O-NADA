package pe.edu.upeu.propiedad.repository;

import pe.edu.upeu.propiedad.entity.Propiedad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PropiedadRepository extends JpaRepository<Propiedad, Long> {

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Propiedad p WHERE p.id = :id")
    java.util.Optional<Propiedad> bloquear(@org.springframework.data.repository.query.Param("id") Long id);

    @Query("SELECT p FROM Propiedad p JOIN FETCH p.tipoPropiedad")
    List<Propiedad> findAllConTipoPropiedad();
}