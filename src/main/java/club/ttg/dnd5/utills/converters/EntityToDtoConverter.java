package club.ttg.dnd5.utills.converters;

public interface EntityToDtoConverter<E, D> {
    D convertToDto(E entity);
    E convertToEntity(D dto);
}