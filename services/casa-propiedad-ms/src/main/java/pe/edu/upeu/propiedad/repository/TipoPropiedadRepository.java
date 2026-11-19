package pe.edu.upeu.propiedad.repository;

import pe.edu.upeu.propiedad.entity.TipoPropiedad;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TipoPropiedadRepository extends JpaRepository<TipoPropiedad, Long> {
}