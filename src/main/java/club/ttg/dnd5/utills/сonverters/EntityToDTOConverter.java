package club.ttg.dnd5.utills.сonverters;

public interface EntityToDTOConverter<E, D> {
    D convertToDTO(E entity);
    E convertToEntity(D dto);
}