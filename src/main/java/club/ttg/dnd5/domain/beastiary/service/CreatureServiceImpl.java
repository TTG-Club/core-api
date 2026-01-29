package club.ttg.dnd5.domain.beastiary.service;

import club.ttg.dnd5.domain.beastiary.CreatureGroupType;
import club.ttg.dnd5.domain.beastiary.model.Creature;
import club.ttg.dnd5.domain.beastiary.model.QCreature;
import club.ttg.dnd5.domain.beastiary.repository.CreatureRepository;
import club.ttg.dnd5.domain.beastiary.rest.dto.CreatureDetailResponse;
import club.ttg.dnd5.domain.beastiary.rest.dto.CreatureRequest;
import club.ttg.dnd5.domain.beastiary.rest.dto.CreatureShortResponse;
import club.ttg.dnd5.domain.beastiary.rest.mapper.CreatureMapper;
import club.ttg.dnd5.domain.common.dictionary.Alignment;
import club.ttg.dnd5.domain.common.dictionary.ChallengeRating;
import club.ttg.dnd5.domain.common.dictionary.Size;
import club.ttg.dnd5.domain.common.model.Gallery;
import club.ttg.dnd5.domain.common.model.SectionType;
import club.ttg.dnd5.domain.common.repository.GalleryRepository;
import club.ttg.dnd5.domain.common.rest.dto.container.ContainerResponse;
import club.ttg.dnd5.domain.common.rest.dto.container.MetadataResponse;
import club.ttg.dnd5.domain.common.rest.dto.container.OrderResponse;
import club.ttg.dnd5.domain.common.rest.dto.container.PaginationResponse;
import club.ttg.dnd5.domain.filter.model.SearchBody;
import club.ttg.dnd5.domain.source.service.SourceService;
import club.ttg.dnd5.exception.EntityExistException;
import club.ttg.dnd5.exception.EntityNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.querydsl.core.types.OrderSpecifier;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

@RequiredArgsConstructor
@Service
public class CreatureServiceImpl implements CreatureService {
    private final CreatureRepository creatureRepository;
    private final CreatureQueryDslSearchService creatureQueryDslSearchService;
    private final SourceService sourceService;
    private final GalleryRepository galleryRepository;
    private final CreatureMapper creatureMapper;
    private final ObjectMapper objectMapper;

    @Override
    public Boolean existOrThrow(final String url) {
        if (!creatureRepository.existsById(url)) {
            throw new EntityNotFoundException(String.format("Существо с url %s не существует", url));
        }
        return true;
    }

    @Override
    public List<CreatureShortResponse> search(final String searchLine, final String filters) {
        var searchBody = SearchBody.parse(filters, objectMapper);
        return search(searchLine, searchBody);
    }

    @Override
    public ContainerResponse<CreatureShortResponse> search(final String searchLine,
                                                           final String filters,
                                                           final CreatureGroupType group,
                                                           final String sort,
                                                           final long limit,
                                                           final long skip) {
        var searchBody = SearchBody.parse(filters, objectMapper);

        var builder = ContainerResponse.<CreatureShortResponse>builder();
        Map<String, OrderResponse<CreatureShortResponse>> result = new TreeMap<>(CrComparator.INSTANCE);
        switch (group)
        {
            case CR ->
                creatureQueryDslSearchService
                        .search(searchLine, searchBody, new OrderSpecifier[] { QCreature.creature.experience.asc(), QCreature.creature.name.asc() }, skip, limit)
                        .forEach(creature ->
                        {
                            String cr = ChallengeRating.getCr(creature.getExperience());

                            OrderResponse<CreatureShortResponse> bucket = result.computeIfAbsent(
                                    cr,
                                    k -> OrderResponse.<CreatureShortResponse>builder()
                                            .items(new ArrayList<>())
                                            .build()
                            );
                            bucket.setOrder(ChallengeRating.getCr(cr).ordinal());
                            bucket.setLabel(group.getName() + ": " + cr);
                            bucket.getItems().add(creatureMapper.toShort(creature));
                        });
            case SIZE -> creatureQueryDslSearchService
                    .search(searchLine, searchBody, new OrderSpecifier[] {  }, skip, limit)
                    .forEach(creature ->
                    {
                        for (Size size: creature.getSizes().getValues())
                        {
                            OrderResponse<CreatureShortResponse> bucket = result.computeIfAbsent(
                                    size.getName(),
                                    k -> OrderResponse.<CreatureShortResponse>builder()
                                            .items(new ArrayList<>())
                                            .build()
                            );
                            bucket.setOrder(size.ordinal());
                            bucket.setLabel(group.getName() + ": " + size.getName());
                            bucket.getItems().add(creatureMapper.toShort(creature));
                        }
                    });
            case TYPE -> creatureQueryDslSearchService
                    .search(searchLine, searchBody, new OrderSpecifier[] { }, skip, limit)
                    .forEach(creature ->
                    {
                        for (var type: creature.getTypes().getValues())
                        {
                            OrderResponse<CreatureShortResponse> bucket = result.computeIfAbsent(
                                    type.getName(),
                                    k -> OrderResponse.<CreatureShortResponse>builder()
                                            .items(new ArrayList<>())
                                            .build()
                            );
                            bucket.setOrder(type.ordinal());
                            bucket.setLabel(group.getName() + ": " + type.getName());
                            bucket.getItems().add(creatureMapper.toShort(creature));
                        }
                    });
            default -> throw new UnsupportedOperationException("Не поддерживаемый тип группировки");
        }
        builder.data(result.values());
        return builder
                .metadata(MetadataResponse.builder()
                        .pagination(PaginationResponse.builder()
                                .skip(skip)
                                .limit(limit)
                                .build())
                        .build())
                .build();
    }

    @Override
    public List<CreatureShortResponse> search(final String searchLine, final SearchBody searchBody) {
        return creatureQueryDslSearchService.search(searchLine, searchBody)
                .stream()
                .map(creatureMapper::toShort)
                .toList();
    }

    @Override
    public CreatureDetailResponse findDetailedByUrl(final String url) {
        var response = creatureMapper.toDetail(findByUrl(url));
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
        var beast = creatureMapper.toEntity(request, book);
        return creatureRepository.save(beast).getUrl();
    }

    @Secured("ADMIN")
    @Transactional
    @Override
    public String update(final String url, final CreatureRequest request) {
        findByUrl(url);
        if (!url.equalsIgnoreCase(request.getUrl())) {
            creatureRepository.deleteById(url);
        }
        var book = sourceService.findByUrl(request.getSource().getUrl());
        var beast = creatureMapper.toEntity(request, book);
        if (!Objects.equals(url, request.getUrl())) {
            creatureRepository.deleteById(url);
            creatureRepository.flush();
        }
        galleryRepository.deleteByUrlAndType(request.getUrl(), SectionType.BESTIARY);

        saveGallery(request.getUrl(), request.getGallery());
        return creatureRepository.save(beast).getUrl();
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

    private Creature findByUrl(String url) {
        return creatureRepository.findById(url)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Существо с URL: %s не существует", url)));
    }
}
