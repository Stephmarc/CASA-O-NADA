package pe.edu.upeu.pago;
import org.junit.jupiter.api.*;import static org.junit.jupiter.api.Assertions.*;import static org.mockito.Mockito.*;import java.util.*;
import pe.edu.upeu.pago.service.*;import pe.edu.upeu.pago.repository.*;import pe.edu.upeu.pago.messaging.*;import pe.edu.upeu.pago.mapper.*;import pe.edu.upeu.pago.entity.*;import pe.edu.upeu.pago.event.*;import pe.edu.upeu.pago.exception.*;
class PagoServiceTest {
 PagoRepository repo;PagoEventosProducer producer;PagoServiceImpl service;
 @BeforeEach void setup(){repo=mock(PagoRepository.class);producer=mock(PagoEventosProducer.class);service=new PagoServiceImpl(repo,producer,mock(PagoMapper.class));}
 @Test void duplicadoNoGeneraOtroPago(){when(repo.findByReservaId(1L)).thenReturn(Optional.of(new Pago()));service.procesar(ReservaCreadaEvento.builder().reservaId(1L).build());verify(repo,never()).save(any());verifyNoInteractions(producer);}
 @Test void pagoAjenoNoSeExpone(){when(repo.findById(1L)).thenReturn(Optional.of(Pago.builder().idCliente("otro").build()));assertThrows(ResourceNotFoundException.class,()->service.obtener(1L,"yo"));}
 @Test void rechazoPublicaCompensacion(){when(repo.findByReservaId(1L)).thenReturn(Optional.empty());when(repo.save(any())).thenAnswer(a->a.getArgument(0));service.procesar(ReservaCreadaEvento.builder().reservaId(1L).idCliente("yo").metodoPago("TARJETA_RECHAZADA").total(java.math.BigDecimal.TEN).build());verify(producer).publicarTrasCommit(argThat(e->e.getTipoEvento().equals("pago.fallido")));}
}
