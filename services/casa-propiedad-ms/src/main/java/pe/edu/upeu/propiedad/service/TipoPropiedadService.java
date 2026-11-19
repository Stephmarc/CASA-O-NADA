package pe.edu.upeu.propiedad.service;

import pe.edu.upeu.propiedad.dto.TipoPropiedadRequest;
import pe.edu.upeu.propiedad.dto.TipoPropiedadResponse;
import pe.edu.upeu.propiedad.entity.TipoPropiedad;
import pe.edu.upeu.propiedad.exception.ResourceNotFoundException;
import pe.edu.upeu.propiedad.mapper.TipoPropiedadMapper;
import pe.edu.upeu.propiedad.repository.TipoPropiedadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TipoPropiedadService {

    private final TipoPropiedadRepository tipoPropiedadRepository;
    private final TipoPropiedadMapper tipoPropiedadMapper;

    public List<TipoPropiedadResponse> listar() {
        return tipoPropiedadRepository.findAll().stream()
                .map(tipoPropiedadMapper::toResponse)
                .toList();
    }

    public TipoPropiedadResponse obtener(Long id) {
        return tipoPropiedadMapper.toResponse(buscarOFallar(id));
    }

    public TipoPropiedadResponse crear(TipoPropiedadRequest request) {
        TipoPropiedad tipoPropiedad = tipoPropiedadMapper.toEntity(request);
        return tipoPropiedadMapper.toResponse(tipoPropiedadRepository.save(tipoPropiedad));
    }

    public TipoPropiedadResponse actualizar(Long id, TipoPropiedadRequest request) {
        TipoPropiedad tipoPropiedad = buscarOFallar(id);
        tipoPropiedad.setNombre(request.getNombre());
        tipoPropiedad.setDescripcion(request.getDescripcion());
        return tipoPropiedadMapper.toResponse(tipoPropiedadRepository.save(tipoPropiedad));
    }

    public void eliminar(Long id) {
        tipoPropiedadRepository.delete(buscarOFallar(id));
    }

    private TipoPropiedad buscarOFallar(Long id) {
        return tipoPropiedadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TipoPropiedad no encontrada: " + id));
    }
}