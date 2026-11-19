package pe.edu.upeu.propiedad;
import org.junit.jupiter.api.*;import static org.junit.jupiter.api.Assertions.*;import static org.mockito.Mockito.*;import java.util.Optional;
import pe.edu.upeu.propiedad.service.*;import pe.edu.upeu.propiedad.repository.*;import pe.edu.upeu.propiedad.mapper.*;import pe.edu.upeu.propiedad.entity.*;import pe.edu.upeu.propiedad.exception.*;
class DisponibilidadTest {
 PropiedadRepository repo;PropiedadService service;
 @BeforeEach void setup(){repo=mock(PropiedadRepository.class);service=new PropiedadService(repo,mock(PropiedadMapper.class),mock(TipoPropiedadRepository.class));}
 @Test void rechazaCantidadNegativa(){assertThrows(IllegalArgumentException.class,()->service.descontarStock(1L,-1));verifyNoInteractions(repo);}
 @Test void noReservaPropiedadOcupada(){when(repo.bloquear(1L)).thenReturn(Optional.of(Propiedad.builder().activo(true).stock(0).build()));assertThrows(StockInsuficienteException.class,()->service.descontarStock(1L,1));}
 @Test void reservaUnaSolaDisponibilidad(){Propiedad p=Propiedad.builder().activo(true).stock(1).build();when(repo.bloquear(1L)).thenReturn(Optional.of(p));service.descontarStock(1L,1);assertEquals(0,p.getStock());verify(repo).save(p);}
}
