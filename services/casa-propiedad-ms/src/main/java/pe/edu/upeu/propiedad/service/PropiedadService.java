package pe.edu.upeu.propiedad.service;

import pe.edu.upeu.propiedad.dto.PropiedadRequest;
import pe.edu.upeu.propiedad.dto.PropiedadResponse;
import pe.edu.upeu.propiedad.entity.TipoPropiedad;
import pe.edu.upeu.propiedad.entity.Propiedad;
import pe.edu.upeu.propiedad.exception.ResourceNotFoundException;
import pe.edu.upeu.propiedad.mapper.PropiedadMapper;
import pe.edu.upeu.propiedad.repository.TipoPropiedadRepository;
import pe.edu.upeu.propiedad.repository.PropiedadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.propiedad.exception.StockInsuficienteException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PropiedadService {

    private final PropiedadRepository propiedadRepository;
    private final PropiedadMapper propiedadMapper;
    private final TipoPropiedadRepository tipoPropiedadRepository;

    public List<PropiedadResponse> listar() {
        return propiedadRepository.findAllConTipoPropiedad().stream()
                .map(propiedadMapper::toResponse)
                .toList();
    }

    public PropiedadResponse obtener(Long id) {
        return propiedadMapper.toResponse(buscarOFallar(id));
    }

    public PropiedadResponse crear(PropiedadRequest request) {
        Propiedad propiedad = propiedadMapper.toEntity(request);
        propiedad.setTipoPropiedad(buscarTipoPropiedadOFallar(request.getTipoPropiedadId()));
        return propiedadMapper.toResponse(propiedadRepository.save(propiedad));
    }

    public PropiedadResponse actualizar(Long id, PropiedadRequest request) {
        Propiedad propiedad = buscarOFallar(id);
        propiedad.setDireccion(request.getDireccion());
        propiedad.setCiudad(request.getCiudad());
        propiedad.setAreaM2(request.getAreaM2());
        propiedad.setDormitorios(request.getDormitorios());
        propiedad.setNombre(request.getNombre());
        propiedad.setDescripcion(request.getDescripcion());
        propiedad.setPrecio(request.getPrecio());
        propiedad.setActivo(request.getActivo());
        propiedad.setStock(request.getStock());
        propiedad.setTipoPropiedad(buscarTipoPropiedadOFallar(request.getTipoPropiedadId()));
        return propiedadMapper.toResponse(propiedadRepository.save(propiedad));
    }

    public void eliminar(Long id) {
        propiedadRepository.delete(buscarOFallar(id));
    }

    private Propiedad buscarOFallar(Long id) {
        return propiedadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Propiedad no encontrado: " + id));
    }

    private TipoPropiedad buscarTipoPropiedadOFallar(Long tipoPropiedadId) {
        return tipoPropiedadRepository.findById(tipoPropiedadId)
                .orElseThrow(() -> new ResourceNotFoundException("TipoPropiedad no encontrada: " + tipoPropiedadId));
    }
    
    @Transactional
    public void descontarStock(Long id, Integer cantidad) {
        if (cantidad == null || cantidad != 1) throw new IllegalArgumentException("Solo se reserva una propiedad por operación");
        Propiedad propiedad = propiedadRepository.bloquear(id).orElseThrow(() -> new ResourceNotFoundException("Propiedad no encontrada"));
        if (!Boolean.TRUE.equals(propiedad.getActivo()) || propiedad.getStock() < cantidad) {
            throw new StockInsuficienteException("Stock insuficiente para el propiedad: " + id);
        }
        propiedad.setStock(propiedad.getStock() - cantidad);
        propiedadRepository.save(propiedad);
    }

    @Transactional
    public void restaurarStock(Long id, Integer cantidad) {
        Propiedad propiedad = buscarOFallar(id);
        if (cantidad == null || cantidad != 1) throw new IllegalArgumentException("Cantidad inválida");
        propiedad.setStock(1);
        propiedadRepository.save(propiedad);
    }


}