package club.ttg.dnd5.domain.clazz.rest.mapper;

//@Mapper(componentModel = "spring")
//public interface ClassFeatureMapper {
//
//    @Mapping(source = "name", target = "name.rus")
//    @Mapping(source = "english", target = "name.eng")
//    @Mapping(source = "alternative", target = "name.alt")
//    @Mapping(source = "genitive", target = "name.genitive")
//    @Mapping(source = "shortName", target = "name.shortName")
//    ClassFeatureDto toDto(ClassFeature classFeature);
//
//    @Mapping(source = "name.rus", target = "name")
//    @Mapping(source = "name.eng", target = "english")
//    @Mapping(source = "name.alt", target = "alternative")
//    @Mapping(source = "name.genitive", target = "genitive")
//    @Mapping(source = "name.shortName", target = "shortName")
//    ClassFeature toEntity(ClassFeatureDto classFeatureDto);
//
//    default NameDto map(String value) {
//        return new NameDto(value, null, null, null, null);
//    }
//
//    default String map(NameDto nameDto) {
//        return nameDto != null ? nameDto.getEng() : null;
//    }
//}
//
