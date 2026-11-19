package pe.edu.upeu.propiedad.mapper;

import pe.edu.upeu.propiedad.dto.TipoPropiedadRequest;
import pe.edu.upeu.propiedad.dto.TipoPropiedadResponse;
import pe.edu.upeu.propiedad.entity.TipoPropiedad;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TipoPropiedadMapper {

    TipoPropiedadResponse toResponse(TipoPropiedad tipoPropiedad);

    @Mapping(target = "id", ignore = true)
    TipoPropiedad toEntity(TipoPropiedadRequest request);
}
