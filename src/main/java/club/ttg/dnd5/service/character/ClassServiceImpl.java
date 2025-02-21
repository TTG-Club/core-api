package club.ttg.dnd5.service.character;

import club.ttg.dnd5.dto.character.ClassDto;
import club.ttg.dnd5.dto.character.ClassFeatureDto;
import club.ttg.dnd5.dto.engine.SearchRequest;
import club.ttg.dnd5.exception.ApiException;
import club.ttg.dnd5.exception.EntityExistException;
import club.ttg.dnd5.exception.EntityNotFoundException;
import club.ttg.dnd5.model.character.ClassCharacter;
import club.ttg.dnd5.model.character.ClassFeature;
import club.ttg.dnd5.repository.character.ClassRepository;
import club.ttg.dnd5.utills.Converter;
import club.ttg.dnd5.utills.character.ClassConverter;
import club.ttg.dnd5.utills.character.ClassFeatureConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class ClassServiceImpl implements ClassService {
    private final ClassRepository classRepository;

    @Override
    public boolean exist(final String url) {
        return classRepository.existsById(url);
    }

    @Override
    public Collection<ClassDto> getClasses(final SearchRequest request) {
        return classRepository.findAllClasses()
                .stream()
                .map(classes -> toDTO(classes, true))
                .toList();
    }

    @Override
    public Collection<ClassDto> getSubClasses(final String url) {
        return classRepository.findAllSubclasses(url)
                .stream()
                .map(classes -> toDTO(classes, true))
                .toList();
    }

    @Override
    public ClassDto addParent(final String classUrl, final String classParentUrl) {
        var classCharacter = findByUrl(classUrl);
        var parent = findByUrl(classParentUrl);
        //на этапе save, мы делаем ссылку на самого себя, если это родитель
        //тут же мы проверяем это утверждение.
        if (parent.getParent().equals(parent)) {
            classCharacter.setParent(parent);

            Optional.ofNullable(parent.getSubClasses())
                    .orElseGet(() -> {
                        parent.setSubClasses((new ArrayList<>()));
                        return parent.getSubClasses();
                    })
                    .add(classCharacter);

            return toDTO(classRepository.save(classCharacter), false);
        } else {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "This is not a parent Class");
        }
    }

    @Override
    public ClassDto addFeature(final String classUrl, final ClassFeatureDto featureDto) {
        var classCharacter =  findByUrl(classUrl);
        var feature = ClassFeatureConverter.toEntityFeature(featureDto);
        classCharacter.getFeatures().add(feature);
        return toDTO(classRepository.save(classCharacter));
    }

    @Override
    public ClassDto getClass(final String url) {
        return toDTO(classRepository.findById(url)
                .orElseThrow(EntityNotFoundException::new));
    }

    @Transactional
    @Override
    public ClassDto addClass(final ClassDto request) {
        classRepository.findById(
                request.getUrl()).ifPresent(c -> {
                    throw new EntityExistException();
                });
        var saved = classRepository.save(toEntity(request));
        return toDTO(saved);
    }

    @Transactional
    @Override
    public ClassDto updateClass(final String url, final ClassDto request) {
        var entity = findByUrl(url);
        if (!url.equals(request.getUrl())) {
            classRepository.deleteById(url);
        }
        var updated = classRepository.save(toEntity(entity, request));
        return toDTO(updated);
    }

    private ClassDto toDTO(ClassCharacter classCharacter) {
        return toDTO(classCharacter, false);
    }

    private ClassDto toDTO(ClassCharacter classCharacter, boolean hideDetails) {
        ClassDto dto = new ClassDto();
        if (hideDetails) {
            Converter.MAP_ENTITY_TO_BASE_DTO_WITH_HIDE_DETAILS.apply(dto, classCharacter);
        } else {
            ClassConverter.MAP_ENTITY_TO_DTO_.apply(dto, classCharacter);
            Converter.MAP_ENTITY_TO_BASE_DTO.apply(dto, classCharacter);
            Converter.MAP_ENTITY_SOURCE_TO_DTO_SOURCE.apply(dto.getSourceDTO(), classCharacter);
            handleParentAndChild(classCharacter, dto);
            Collection<ClassFeature> features = classCharacter.getFeatures();
            if (features != null) {
                dto.setFeatures(ClassFeatureConverter.convertEntityFeatureIntoDTOFeature(features));
            }
        }
        return dto;
    }

    private void handleParentAndChild(ClassCharacter classCharacter, ClassDto dto) {
        if (classCharacter.getParent() != null) {
            dto.setParentUrl(classCharacter.getParent().getUrl());
        }

        if (classCharacter.getSubClasses() != null) {
            dto.setSubSpeciesUrls(classCharacter.getSubClasses()
                    .stream()
                    .map(ClassCharacter::getUrl)
                    .toList());
        }
    }

    private ClassCharacter toEntity(ClassDto dto) {
        return toEntity(new ClassCharacter(), dto);
    }

    private ClassCharacter toEntity(ClassCharacter classCharacter, ClassDto dto) {
        classCharacter.setUrl(dto.getUrl());
        Converter.MAP_BASE_DTO_TO_ENTITY_NAME.apply(dto, classCharacter);
        Converter.MAP_DTO_SOURCE_TO_ENTITY_SOURCE.apply(dto.getSourceDTO(), classCharacter);
        ClassConverter.MAP_DTO_TO_ENTITY.apply(dto, classCharacter);
        if (dto.getParentUrl() != null) {
            classCharacter.setParent(dto.getParentUrl().equals(dto.getUrl()) ? null : findByUrl(dto.getParentUrl()));
        }
        fillClass(dto, classCharacter);
        saveClassFeatures(dto, classCharacter);
        return classCharacter;
    }

    private void saveClassFeatures(final ClassDto dto, final ClassCharacter classCharacter) {
        Collection<ClassFeature> features = dto.getFeatures().stream()
                .map(ClassFeatureConverter::toEntityFeature)
                .toList();
        classCharacter.setFeatures(features);
    }

    private void fillClass(ClassDto dto, ClassCharacter classCharacter) {
        Optional.ofNullable(dto.getSubSpeciesUrls())
                .ifPresent(urls -> classCharacter.setSubClasses(
                        urls.stream()
                        .map(this::findByUrl)
                        .toList()));
    }

    private ClassCharacter findByUrl(String url) {
        return classRepository.findById(url)
                .orElseThrow(() -> new EntityNotFoundException("Entity not found with URL: " + url));
    }
}
