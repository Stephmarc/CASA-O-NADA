package pe.edu.upeu.reserva;
import org.junit.jupiter.api.*;import static org.junit.jupiter.api.Assertions.*;import static org.mockito.Mockito.*;
import java.util.*;import java.math.BigDecimal;
import pe.edu.upeu.reserva.service.*;import pe.edu.upeu.reserva.repository.*;import pe.edu.upeu.reserva.messaging.*;import pe.edu.upeu.reserva.dto.*;import pe.edu.upeu.reserva.entity.*;import pe.edu.upeu.reserva.exception.*;
class ReservaServiceTest {
 ReservaRepository repo;PropiedadConsultaService propiedades;ReservaEventosProducer producer;ReservaServiceImpl service;
 @BeforeEach void setup(){repo=mock(ReservaRepository.class);propiedades=mock(PropiedadConsultaService.class);producer=mock(ReservaEventosProducer.class);service=new ReservaServiceImpl(repo,propiedades,producer);}
 @Test void creaConPrecioDelServicioYDuenoAutenticado(){
  when(propiedades.consultarPropiedad(1L)).thenReturn(PropiedadDto.builder().id(1L).nombre("Casa").precio(new BigDecimal("1500")).activo(true).build());
  when(propiedades.descontarStock(1L,1)).thenReturn(true);
  when(repo.save(any())).thenAnswer(a->{Reserva r=a.getArgument(0);r.setId(10L);return r;});
  var r=service.crear(ReservaRequest.builder().metodoPago("TARJETA").detalles(List.of(DetalleReservaRequest.builder().idPropiedad(1L).cantidad(1).build())).build(),"usuario-real");
  assertEquals("usuario-real",r.getIdCliente());assertEquals("PENDIENTE_PAGO",r.getEstado());assertEquals(new BigDecimal("1500"),r.getTotal());verify(producer).publicarTrasCommit(any());
 }
 @Test void noExponeReservaAjena(){when(repo.findById(1L)).thenReturn(Optional.of(Reserva.builder().idCliente("otro").build()));assertThrows(ResourceNotFoundException.class,()->service.findById(1L,"yo"));}
 @Test void compensaSoloUnaVez(){Reserva r=Reserva.builder().id(1L).estado(EstadoReserva.PENDIENTE_PAGO).detalles(List.of(ReservaDetalle.builder().idPropiedad(2L).cantidad(1).build())).build();when(repo.bloquear(1L)).thenReturn(Optional.of(r));service.compensar(1L);service.compensar(1L);assertEquals(EstadoReserva.CANCELADA,r.getEstado());verify(propiedades,times(1)).restaurarStock(2L,1);}
 @Test void eventoTardioNoReviveReservaCancelada(){Reserva r=Reserva.builder().estado(EstadoReserva.CANCELADA).build();when(repo.bloquear(1L)).thenReturn(Optional.of(r));service.marcarPagada(1L);assertEquals(EstadoReserva.CANCELADA,r.getEstado());}
 @Test void compensacionFallidaPermanecePendiente(){Reserva r=Reserva.builder().estado(EstadoReserva.PENDIENTE_PAGO).detalles(List.of(ReservaDetalle.builder().idPropiedad(2L).cantidad(1).build())).build();when(repo.bloquear(1L)).thenReturn(Optional.of(r));doThrow(new IllegalStateException("offline")).when(propiedades).restaurarStock(2L,1);assertThrows(IllegalStateException.class,()->service.compensar(1L));assertEquals(EstadoReserva.PENDIENTE_PAGO,r.getEstado());}
}
