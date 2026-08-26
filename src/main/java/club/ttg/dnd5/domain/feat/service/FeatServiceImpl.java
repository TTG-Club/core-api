package club.ttg.dnd5.domain.feat.service;

import club.ttg.dnd5.domain.background.repository.BackgroundRepository;
import club.ttg.dnd5.domain.common.model.EntityRef;
import club.ttg.dnd5.domain.common.service.GrantedSpellResolver;
import club.ttg.dnd5.domain.feat.model.FeatCategory;
import club.ttg.dnd5.domain.feat.model.mechanics.FeatMechanics;
import club.ttg.dnd5.domain.common.model.mechanics.SpellGrant;
import club.ttg.dnd5.domain.common.model.mechanics.SpellListExpansion;
import club.ttg.dnd5.domain.common.model.mechanics.SpellListGroup;
import club.ttg.dnd5.domain.feat.rest.dto.FeatSelectResponse;
import club.ttg.dnd5.domain.spell.rest.dto.SpellShortResponse;
import club.ttg.dnd5.domain.source.service.SourceService;
import club.ttg.dnd5.domain.feat.rest.dto.FeatDetailResponse;
import club.ttg.dnd5.domain.feat.rest.dto.FeatGrantedSpellResponse;
import club.ttg.dnd5.domain.feat.rest.dto.FeatRequest;
import club.ttg.dnd5.domain.feat.rest.dto.FeatShortResponse;
import club.ttg.dnd5.domain.feat.rest.dto.FeatSpellListGroupResponse;
import club.ttg.dnd5.domain.feat.rest.mapper.FeatMapper;
import club.ttg.dnd5.exception.EntityExistException;
import club.ttg.dnd5.exception.EntityNotFoundException;
import club.ttg.dnd5.domain.feat.model.Feat;
import club.ttg.dnd5.domain.feat.repository.FeatRepository;
import club.ttg.dnd5.domain.revision.model.RevisionOperation;
import club.ttg.dnd5.domain.revision.service.EntityRevisionService;
import club.ttg.dnd5.util.SwitchLayoutUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class FeatServiceImpl implements FeatService {
    public static final String REVISION_ENTITY_TYPE = "feat";

    private final FeatRepository featRepository;
    private final BackgroundRepository backgroundRepository;
    private final FeatQueryDslSearchService featQueryDslSearchService;
    private final SourceService sourceService;
    private final FeatMapper featMapper;
    private final GrantedSpellResolver grantedSpellResolver;
    private final EntityRevisionService revisionService;

    @Override
    public FeatDetailResponse getFeat(final String featUrl) {
        var response = featMapper.toDetail(findByUrl(featUrl));
        var backgrounds = backgroundRepository.findVisibleByFeatUrl(featUrl)
                .stream()
                .map(featMapper::toBackgroundDto)
                .toList();
        response.setBackgrounds(backgrounds.isEmpty() ? null : backgrounds);
        response.setGrantedSpells(resolveGrantedSpells(response));
        var spellListGroups = resolveSpellListGroups(response);
        response.setSpellListGroups(spellListGroups);
        response.setSpellListSpells(flattenSpellList(spellListGroups));
        return response;
    }

    /**
     * Дополняет выдаваемые чертой заклинания данными справочника: в механике они лежат
     * ссылками, а потребителю нужны круг и школа — лист персонажа без круга не положит
     * заклинание в книгу.
     *
     * <p>Ненайденное заклинание пропускается, а не роняет ответ. У вида такая ссылка —
     * запись связующей таблицы, и её отсутствие означает битые данные; здесь же это
     * свободный JSONB, набранный руками в редакторе, и опечатка в url не должна ронять
     * страницу черты целиком.</p>
     *
     * <p>Вместе с записью едет требуемый уровень: заклинание, которое черта открывает на
     * третьем уровне, страница обязана показать иначе, чем доступное сразу.</p>
     *
     * @param response деталь черты с разобранной механикой.
     * @return выдаваемые заклинания с данными справочника; null — черта их не выдаёт.
     */
    private Collection<FeatGrantedSpellResponse> resolveGrantedSpells(final FeatDetailResponse response) {
        var granted = Optional.ofNullable(response.getMechanics())
                .map(FeatMechanics::getSpells)
                .map(SpellGrant::getSpells)
                .orElse(List.of());

        if (CollectionUtils.isEmpty(granted)) {
            return null;
        }

        var spellsByUrl = grantedSpellResolver.shortSpellsByUrl(granted);

        var result = granted.stream()
                .filter(Objects::nonNull)
                .filter(ref -> spellsByUrl.containsKey(ref.getUrl()))
                .map(ref -> new FeatGrantedSpellResponse(spellsByUrl.get(ref.getUrl()),
                        ref.getRequiredLevel()))
                .toList();

        return result.isEmpty() ? null : result;
    }

    /**
     * Все заклинания списка подряд — из уже разобранных списков доступа.
     *
     * <p>Считается из них, а не отдельным разбором: второй проход означал бы второй запрос
     * в справочник и вторую точку, где плоское поле могло разойтись со списками.</p>
     *
     * <p>Заклинание, стоящее сразу в двух списках, попадает сюда один раз: поле нужно для
     * таблицы по кругам, и дубль показал бы там лишнюю строку.</p>
     *
     * @param groups разобранные списки доступа; null — черта список не расширяет.
     * @return заклинания списка с данными справочника; null — расширять нечем.
     */
    private Collection<SpellShortResponse> flattenSpellList(
            final Collection<FeatSpellListGroupResponse> groups) {
        if (CollectionUtils.isEmpty(groups)) {
            return null;
        }

        var result = groups.stream()
                .map(FeatSpellListGroupResponse::getSpells)
                .flatMap(Collection::stream)
                .distinct()
                .toList();

        return result.isEmpty() ? null : result;
    }

    /**
     * Раскладывает заклинания списка по спискам доступа, дополняя их справочником.
     *
     * <p>Список без единого найденного заклинания выбрасывается целиком: пустая ступень на
     * странице выглядела бы как «на этом уровне ничего не открывается», хотя на деле там
     * опечатка в url.</p>
     *
     * @param response деталь черты с разобранной механикой.
     * @return списки с данными справочника; null — черта список не расширяет.
     */
    private Collection<FeatSpellListGroupResponse> resolveSpellListGroups(final FeatDetailResponse response) {
        var groups = spellListGroups(response);

        if (groups.isEmpty()) {
            return null;
        }

        var spellsByUrl = grantedSpellResolver.shortSpellsByUrl(groups.stream()
                .map(SpellListGroup::getSpells)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .toList());

        var result = groups.stream()
                .map(group -> {
                    var spells = Optional.ofNullable(group.getSpells()).orElse(List.<EntityRef>of()).stream()
                            .filter(Objects::nonNull)
                            .map(ref -> spellsByUrl.get(ref.getUrl()))
                            .filter(Objects::nonNull)
                            .toList();
                    return spells.isEmpty() ? null : new FeatSpellListGroupResponse(
                            group.getRequiredLevel(), group.getCount(), spells);
                })
                .filter(Objects::nonNull)
                .toList();

        return result.isEmpty() ? null : result;
    }

    /** Списки блока с поправкой на прежнюю плоскую форму — см. {@link SpellListExpansion#resolveGroups()}. */
    private List<SpellListGroup> spellListGroups(final FeatDetailResponse response) {
        return Optional.ofNullable(response.getMechanics())
                .map(FeatMechanics::getSpellList)
                .map(SpellListExpansion::resolveGroups)
                .orElse(List.of());
    }

    @Secured({"ADMIN", "MODERATOR"})
    @Transactional
    @Override
    @CacheEvict(cacheNames = "countAllMaterials")
    public String addFeat(final FeatRequest dto) {
        if (featRepository.existsById(dto.getUrl())) {
            throw new EntityExistException(String.format("Черта с url %s уже существует", dto.getUrl()));
        }
        var book = sourceService.findByUrl(dto.getSource().getUrl());
        var feat = featMapper.toEntity(dto, book);
        String url = featRepository.save(feat).getUrl();
        revisionService.record(REVISION_ENTITY_TYPE, url, RevisionOperation.CREATE, findFormByUrl(url));
        return url;
    }

    @Secured({"ADMIN", "MODERATOR"})
    @Transactional
    @Override
    public String updateFeat(final String featUrl, final FeatRequest request) {
        var existing = findByUrl(featUrl);
        var book = sourceService.findByUrl(request.getSource().getUrl());

        if (featUrl.equals(request.getUrl())) {
            // url не изменился — обновляем существующую строку (merge вместо delete+insert),
            // чтобы не задеть FK fk_background_on_feat. createdAt переносим, иначе isNew()
            // вернёт true и Spring Data попытается сделать INSERT (дубль ключа).
            featMapper.updateEntity(request, book, existing);
            String url = featRepository.save(existing).getUrl();
            revisionService.record(REVISION_ENTITY_TYPE, url, RevisionOperation.UPDATE, findFormByUrl(url));
            return url;
        }

        // url изменился — создаём черту с новым id, переводим на неё ссылки предысторий
        // и удаляем старую. Порядок важен: новая черта должна существовать до repoint,
        // а старую удаляем уже без ссылок.
        if (featRepository.existsById(request.getUrl())) {
            throw new EntityExistException(String.format("Черта с url %s уже существует", request.getUrl()));
        }
        var feat = featMapper.toEntity(request, book);
        featRepository.saveAndFlush(feat);
        backgroundRepository.repointFeat(featUrl, feat.getUrl());
        featRepository.deleteById(featUrl);
        revisionService.record(REVISION_ENTITY_TYPE, feat.getUrl(), RevisionOperation.UPDATE,
                findFormByUrl(feat.getUrl()));
        return feat.getUrl();
    }

    @Secured({"ADMIN", "MODERATOR"})
    @Transactional
    @Override
    @CacheEvict(cacheNames = "countAllMaterials")
    public String delete(final String featUrl) {
        var entity = findByUrl(featUrl);
        entity.setHiddenEntity(true);
        String url = featRepository.save(entity).getUrl();
        revisionService.record(REVISION_ENTITY_TYPE, url, RevisionOperation.DELETE, findFormByUrl(url));
        return url;
    }

    @Override
    public boolean existOrThrow(final String url) {
        if (!featRepository.existsById(url)) {
            throw new EntityNotFoundException(String.format("Черта с url %s не существует", url));
        }
        return true;
    }

    @Override
    public FeatRequest findFormByUrl(final String url) {
        return featMapper.toRequest(findByUrl(url));
    }

    private Feat findByUrl(String url) {
        return featRepository.findById(url)
                .orElseThrow(() -> new EntityNotFoundException("Черта не найдена по URL: " + url));
    }

    public FeatDetailResponse preview(final FeatRequest request) {
        var book = sourceService.findByUrl(request.getSource().getUrl());
        var response = featMapper.toDetail(featMapper.toEntity(request, book));
        // Как и у сохранённой черты: без дополнения превью показало бы выдаваемые
        // заклинания пустыми, и редактор не увидел бы того, что набрал.
        response.setGrantedSpells(resolveGrantedSpells(response));
        var spellListGroups = resolveSpellListGroups(response);
        response.setSpellListGroups(spellListGroups);
        response.setSpellListSpells(flattenSpellList(spellListGroups));
        return response;
    }

    @Override
    public Collection<FeatSelectResponse> getFeatsSelect(
            final String searchLine,
            final Set<FeatCategory> categories) {
        return featRepository.findBySearchLine(searchLine,
                        SwitchLayoutUtils.switchLayout(searchLine == null ? "" : searchLine),
                        Sort.by("name"))
                .stream()
                .filter(f -> CollectionUtils.isEmpty(categories) || categories.contains(f.getCategory()))
                .map(featMapper::toSelect)
                .toList();
    }
    @Override
    public Collection<FeatShortResponse> search(final club.ttg.dnd5.domain.feat.rest.dto.FeatQueryRequest request)
    {
        var predicate = FeatPredicateBuilder.build(request);
        return featQueryDslSearchService.search(predicate, request.getPage(), request.getPageSize())
                .stream()
                .map(featMapper::toShort)
                .toList();
    }

    @Override
    public Set<Feat> findAllById(final Set<String> urls) {
        if (urls == null || urls.isEmpty()) {
            return Set.of();
        }
        return urls.stream()
                .map(featRepository::getReferenceById)
                .collect(Collectors.toSet());
    }
}
