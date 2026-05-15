package club.ttg.dnd5.domain.beastiary.service;

import club.ttg.dnd5.domain.beastiary.model.Creature;
import club.ttg.dnd5.domain.beastiary.repository.CreatureRepository;
import club.ttg.dnd5.domain.beastiary.rest.dto.CreatureDetailResponse;
import club.ttg.dnd5.domain.beastiary.rest.dto.CreatureRequest;
import club.ttg.dnd5.domain.beastiary.rest.dto.CreatureQueryRequest;
import club.ttg.dnd5.domain.beastiary.rest.dto.CreatureShortResponse;
import club.ttg.dnd5.domain.beastiary.rest.mapper.CreatureMapper;
import club.ttg.dnd5.domain.filter.model.FilterHashCategory;
import club.ttg.dnd5.domain.filter.service.FilterHashService;
import club.ttg.dnd5.domain.source.service.SourceService;
import club.ttg.dnd5.domain.common.dictionary.Alignment;
import club.ttg.dnd5.domain.common.model.Gallery;
import club.ttg.dnd5.domain.common.model.SectionType;
import club.ttg.dnd5.domain.common.repository.GalleryRepository;

import club.ttg.dnd5.domain.filter.model.FilterHashMapping;
import club.ttg.dnd5.domain.filter.repository.FilterHashMappingRepository;
import org.springframework.util.StringUtils;
import club.ttg.dnd5.exception.EntityExistException;
import club.ttg.dnd5.exception.EntityNotFoundException;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;

@RequiredArgsConstructor
@Service
public class CreatureServiceImpl implements CreatureService {
    private final CreatureRepository creatureRepository;
    private final CreatureQueryDslSearchService creatureQueryDslSearchService;
    private final SourceService sourceService;
    private final GalleryRepository galleryRepository;
    private final CreatureMapper creatureMapper;
    private final FilterHashMappingRepository filterHashMappingRepository;
    private final FilterHashService filterHashService;

    @Override
    public Boolean existOrThrow(final String url) {
        if (!creatureRepository.existsById(url)) {
            throw new EntityNotFoundException(String.format("Существо с url %s не существует", url));
        }
        return true;
    }

    @Override
    public List<CreatureShortResponse> search(final CreatureQueryRequest request)
    {
        // Резолв хэшей traits → оригинальные значения
        var traitValues = resolveHashes(request.getTraits());
        // Резолв хэшей tags → оригинальные значения
        var tagValues = resolveHashes(request.getTag());

        var predicate = CreaturePredicateBuilder.build(request, traitValues, tagValues);
        return creatureQueryDslSearchService.search(predicate, request.getPage(), request.getPageSize())
                .stream()
                .map(creatureMapper::toShort)
                .toList();
    }

    private List<String> resolveHashes(final club.ttg.dnd5.dto.base.filters.QueryFilter<String> filter)
    {
        if (filter == null || !filter.isActive() || filter.getValues() == null || filter.getValues().isEmpty())
        {
            return List.of();
        }
        var resolved = filterHashMappingRepository.findAllByHashIn(filter.getValues())
                .stream()
                .map(FilterHashMapping::getValue)
                .collect(java.util.stream.Collectors.toList());

        for (String val : filter.getValues()) {
            if (val.length() != 8) {
                resolved.add(val);
            }
        }
        return resolved;
    }

    @Override
    public CreatureDetailResponse findDetailedByUrl(final String url) {
        var response =   creatureMapper.toDetail(findByUrl(url));
        response.setGallery(galleryRepository.findAllByUrlAndType(url, SectionType.BESTIARY)
                .stream()
                .map(Gallery::getImage)
                .toList());
        return response;
    }

    @Override
    public CreatureRequest findFormByUrl(final String url) {
        return creatureMapper.toRequest(findByUrl(url));
    }

    @Secured("ADMIN")
    @Transactional
    @Override
    public String save(final CreatureRequest request) {
        if (creatureRepository.existsById(request.getUrl())) {
            throw new EntityExistException("Существо уже существует с URL: " + request.getUrl());
        }
        if (request.getAlignment() == null) {
            request.setAlignment(Alignment.WITHOUT);
        }
        saveGallery(request.getUrl(), request.getGallery());
        var book = sourceService.findByUrl(request.getSource().getUrl());
        var creature = creatureMapper.toEntity(request, book);
        persistTagHash(creature);
        return creatureRepository.save(creature).getUrl();
    }

    @Secured("ADMIN")
    @Transactional
    @Override
    public String update(final String url, final CreatureRequest request) {
        var existing = findByUrl(url);
        if (!url.equalsIgnoreCase(request.getUrl())) {
            creatureRepository.deleteById(url);
            creatureRepository.flush();
        }
        var book = sourceService.findByUrl(request.getSource().getUrl());
        var creature = creatureMapper.toEntity(request, book);
        creature.setCreatedAt(existing.getCreatedAt());
        galleryRepository.deleteByUrlAndType(request.getUrl(), SectionType.BESTIARY);

        saveGallery(request.getUrl(), request.getGallery());
        persistTagHash(creature);
        return creatureRepository.save(creature).getUrl();
    }

    @Secured("ADMIN")
    @Transactional
    @Override
    public String delete(final String url) {
        Creature existing = findByUrl(url);
        existing.setHiddenEntity(true);
        return creatureRepository.save(existing).getUrl();
    }

    @Override
    public CreatureDetailResponse preview(final CreatureRequest request) {
        var book = sourceService.findByUrOptional(request.getSource().getUrl());
        if (book.isPresent()) {
            return creatureMapper.toDetail(creatureMapper.toEntity(request, book.get()));
        }
        return creatureMapper.toDetail(creatureMapper.toEntity(request, null));
    }

    private void saveGallery(String url, List<String> gallery) {
        if (!CollectionUtils.isEmpty(gallery)) {
            gallery.forEach(
                    image -> galleryRepository.save(Gallery.builder()
                            .url(url)
                            .type(SectionType.BESTIARY)
                            .image(image)
                            .build()));
        }
    }

    private void persistTagHash(final Creature creature)
    {
        if (creature.getTypes() != null && StringUtils.hasText(creature.getTypes().getText()))
        {
             java.util.Arrays.stream(creature.getTypes().getText().split(","))
                    .map(String::trim)
                    .filter(t -> !t.isEmpty())
                    .forEach(tag -> filterHashService.ensureHash(FilterHashCategory.TAG, tag.toLowerCase()));
        }
    }

    private Creature findByUrl(String url) {
        return creatureRepository.findById(url)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Существо с URL: %s не существует", url)));
    }
}
