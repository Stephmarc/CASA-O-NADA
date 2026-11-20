package pe.edu.upeu.propiedad.mapper;

import pe.edu.upeu.propiedad.dto.PropiedadRequest;
import pe.edu.upeu.propiedad.dto.PropiedadResponse;
import pe.edu.upeu.propiedad.entity.Propiedad;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = TipoPropiedadMapper.class)
public interface PropiedadMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tipoPropiedad", ignore = true)
    @Mapping(target = "idVendedor", ignore = true)
    Propiedad toEntity(PropiedadRequest request);

    PropiedadResponse toResponse(Propiedad propiedad);
}
