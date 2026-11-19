package pe.edu.upeu.cliente;
public interface ClienteRepository extends org.springframework.data.jpa.repository.JpaRepository<Cliente,Long> {
 java.util.Optional<Cliente> findByUsuarioId(String usuarioId);
}
