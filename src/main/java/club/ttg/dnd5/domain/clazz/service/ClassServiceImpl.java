package club.ttg.dnd5.domain.clazz.service;

import club.ttg.dnd5.domain.clazz.rest.dto.ClassDetailResponse;
import club.ttg.dnd5.domain.clazz.rest.dto.ClassFeatureRequest;
import club.ttg.dnd5.domain.clazz.rest.dto.ClassRequest;
import club.ttg.dnd5.domain.clazz.rest.dto.ClassShortResponse;
import club.ttg.dnd5.domain.clazz.rest.mapper.ClassFeatureMapper;
import club.ttg.dnd5.domain.clazz.rest.mapper.ClassMapper;
import club.ttg.dnd5.domain.common.rest.dto.engine.SearchRequest;
import club.ttg.dnd5.exception.ApiException;
import club.ttg.dnd5.exception.EntityExistException;
import club.ttg.dnd5.exception.EntityNotFoundException;
import club.ttg.dnd5.domain.clazz.model.ClassCharacter;
import club.ttg.dnd5.domain.clazz.repository.ClassRepository;
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
    private final ClassMapper classMapper;
    private final ClassFeatureMapper classFeatureMapper;

    @Override
    public boolean exist(final String url) {
        return classRepository.existsById(url);
    }

    @Override
    public Collection<ClassShortResponse> getClasses(final SearchRequest request) {
        return classRepository.findAllClasses()
                .stream()
                .map(classMapper::toShortDto)
                .toList();
    }

    @Override
    public Collection<ClassShortResponse> getSubClasses(final String url) {
        return classRepository.findAllSubclasses(url)
                .stream()
                .map(classMapper::toShortDto)
                .toList();
    }

    @Override
    public ClassDetailResponse addParent(final String classUrl, final String classParentUrl) {
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

            return classMapper.toDetailDto(classRepository.save(classCharacter));
        } else {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "This is not a parent Class");
        }
    }

    @Override
    public ClassDetailResponse addFeature(final String classUrl, final ClassFeatureRequest featureDto) {
        var classCharacter =  findByUrl(classUrl);
        classCharacter.getFeatures().add(classFeatureMapper.toEntity(featureDto));
        return classMapper.toDetailDto((classRepository.save(classCharacter)));
    }

    @Override
    public ClassDetailResponse getClass(final String url) {
        return classMapper.toDetailDto(classRepository.findById(url)
                .orElseThrow(EntityNotFoundException::new));
    }

    @Transactional
    @Override
    public ClassDetailResponse addClass(final ClassRequest request) {
        classRepository.findById(
                request.getUrl()).ifPresent(c -> {
                    throw new EntityExistException();
                });
        var saved = classRepository.save(classMapper.toEntity(request));
        return classMapper.toDetailDto(saved);
    }

    @Transactional
    @Override
    public ClassDetailResponse updateClass(final String url, final ClassRequest request) {
        findByUrl(url);
        if (!url.equals(request.getUrl())) {
            classRepository.deleteById(url);
        }
        var updated = classRepository.save(classMapper.toEntity(request));
        return classMapper.toDetailDto(updated);
    }

    private void handleParentAndChild(ClassCharacter classCharacter, ClassDetailResponse dto) {
        if (classCharacter.getParent() != null) {
            dto.setParentUrl(classCharacter.getParent().getUrl());
        }

        if (classCharacter.getSubClasses() != null) {
            dto.setSubClassUrls(classCharacter.getSubClasses()
                    .stream()
                    .map(ClassCharacter::getUrl)
                    .toList());
        }
    }

    private void fillClass(ClassDetailResponse dto, ClassCharacter classCharacter) {
        Optional.ofNullable(dto.getSubClassUrls())
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
