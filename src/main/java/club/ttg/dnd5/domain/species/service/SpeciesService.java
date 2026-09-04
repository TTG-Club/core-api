package club.ttg.dnd5.domain.species.service;

import club.ttg.dnd5.domain.common.dictionary.SenseType;
import club.ttg.dnd5.domain.common.model.mechanics.GrantedSpellRef;
import club.ttg.dnd5.domain.common.model.mechanics.SenseGrant;
import club.ttg.dnd5.domain.common.model.mechanics.SheetModifiers;
import club.ttg.dnd5.domain.common.service.GrantedSpellResolver;
import club.ttg.dnd5.domain.species.model.mechanics.SpeciesMechanics;
import club.ttg.dnd5.domain.source.service.SourceSavedFilterService;
import club.ttg.dnd5.domain.source.service.SourceService;
import club.ttg.dnd5.domain.species.model.SpeciesFeature;
import club.ttg.dnd5.domain.species.rest.dto.SpeciesQueryRequest;
import club.ttg.dnd5.domain.species.rest.dto.SpeciesFeatureResponse;
import club.ttg.dnd5.domain.species.model.Species;
import club.ttg.dnd5.domain.species.repository.SpeciesRepository;
import club.ttg.dnd5.domain.species.repository.SpeciesInnateSpellView;
import club.ttg.dnd5.domain.species.rest.dto.SpeciesDetailResponse;
import club.ttg.dnd5.domain.species.rest.dto.SpeciesRequest;
import club.ttg.dnd5.domain.species.rest.dto.SpeciesShortResponse;
import club.ttg.dnd5.domain.species.rest.dto.SpeciesInnateSpellRequest;
import club.ttg.dnd5.domain.species.rest.dto.SpeciesInnateSpellResponse;
import club.ttg.dnd5.domain.species.rest.mapper.SpeciesFeatureMapper;
import club.ttg.dnd5.domain.species.rest.mapper.SpeciesMapper;
import club.ttg.dnd5.domain.revision.model.RevisionOperation;
import club.ttg.dnd5.domain.revision.service.EntityRevisionService;
import club.ttg.dnd5.domain.spell.model.Spell;
import club.ttg.dnd5.domain.spell.repository.SpellRepository;
import club.ttg.dnd5.domain.spell.rest.mapper.SpellMapper;
import club.ttg.dnd5.exception.ApiException;
import club.ttg.dnd5.exception.EntityExistException;
import club.ttg.dnd5.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class SpeciesService {
    public static final String REVISION_ENTITY_TYPE = "species";

    private final SpeciesRepository speciesRepository;
    private final SourceService sourceService;
    private final SpeciesQueryDslSearchService speciesQueryDslSearchService;
    private final SpeciesMapper speciesMapper;
    private final SpeciesFeatureMapper speciesFeatureMapper;
    private final SourceSavedFilterService sourceSavedFilterService;
    private final EntityRevisionService revisionService;
    private final SpellRepository spellRepository;
    private final SpellMapper spellMapper;
    private final GrantedSpellResolver grantedSpellResolver;

    public boolean exists(String url) {
        return speciesRepository.existsById(url);
    }

    @Transactional(readOnly = true)
    public SpeciesDetailResponse findById(String url) {
        var species = speciesRepository.findById(url)
                .orElseThrow(() -> new EntityNotFoundException(url));
        var response = speciesMapper.toDetail(species);
        response.setInnateSpells(innateSpellsOf(species));

        if (species.getParent() != null) {
            var features = new ArrayList<SpeciesFeature>();
            Optional.ofNullable(species.getFeatures()).ifPresent(features::addAll);
            Optional.ofNullable(species.getParent().getFeatures()).ifPresent(features::addAll);
            response.setFeatures(speciesFeatureMapper.toResponses(features));
            response.setInnateSpells(mergeInnateSpells(
                    innateSpellsOf(species.getParent()),
                    response.getInnateSpells()
            ));
        }

        applyComputedDarkVision(response, species);
        resolveSpellLists(response);
        return response;
    }

    /**
     * Подставляет записи вида и её умениям списки расширения заклинаний с данными
     * справочника — одним запросом на весь вид, как это делает деталь класса.
     *
     * <p>Расширение — не выдача: врождённые заклинания идут отдельно ({@code innateSpells}),
     * а здесь то, что персонаж лишь может подготовить. Круг нужен и тут: без него лист не
     * покажет заклинание в окне добавления.</p>
     *
     * @param response деталь вида с уже собранными умениями.
     */
    private void resolveSpellLists(SpeciesDetailResponse response) {
        var features = Optional.ofNullable(response.getFeatures()).orElse(List.of());

        var refs = Stream.concat(
                        Stream.of(response.getMechanics()),
                        features.stream().map(SpeciesFeatureResponse::getMechanics))
                .filter(Objects::nonNull)
                .map(SpeciesMechanics::getSpellList)
                .filter(Objects::nonNull)
                .flatMap(expansion -> grantedSpellResolver.spellListRefs(expansion).stream())
                .toList();

        if (refs.isEmpty()) {
            return;
        }

        var spellsByUrl = grantedSpellResolver.shortSpellsByUrl(refs);

        if (response.getMechanics() != null) {
            response.setSpellListGroups(
                    grantedSpellResolver.spellListGroups(response.getMechanics().getSpellList(), spellsByUrl));
        }

        for (SpeciesFeatureResponse feature : features) {
            if (feature.getMechanics() != null) {
                feature.setSpellListGroups(
                        grantedSpellResolver.spellListGroups(feature.getMechanics().getSpellList(), spellsByUrl));
            }
        }
    }

    @Transactional(readOnly = true)
    public Set<Species> findAllById(Collection<String> urls)
    {
        if (urls == null || urls.isEmpty())
        {
            return Set.of();
        }
        return urls.stream()
                .map(speciesRepository::getReferenceById)
                .collect(Collectors.toSet());
    }

    public List<SpeciesShortResponse> search(SpeciesQueryRequest request) {
        var predicate = SpeciesPredicateBuilder.build(request);
        // Подвиды отсекает предикат, а не отбор над страницей: иначе они занимали бы
        // места В странице, и запрос первых тридцати записей отдавал бы горстку видов
        return speciesQueryDslSearchService.search(predicate, request.getPage(), request.getPageSize())
                .stream()
                .map(speciesMapper::toShort)
                .collect(Collectors.toList());
    }

    @Transactional
    @CacheEvict(cacheNames = "countAllMaterials")
    public String save(SpeciesRequest request) {
        if (speciesRepository.existsById(request.getUrl())) {
            throw new EntityExistException(String.format("Вид с url %s уже существует", request.getUrl()));
        }
        String url = saveSpecies(request).getUrl();
        revisionService.record(REVISION_ENTITY_TYPE, url, RevisionOperation.CREATE, findFormByUrl(url));
        return url;
    }

    @Transactional(readOnly = true)
    public List<SpeciesDetailResponse> getLineages(String parentUrl) {
        return speciesRepository.findById(parentUrl)
                .filter(species -> !species.isHiddenEntity())
                .map(speciesRepository::findByParent)
                .orElseThrow(() -> new EntityNotFoundException("Вид не найден для URL: " + parentUrl))
                .stream()
                .map(this::toDetailWithInnateSpells)
                .toList();
    }

    public List<SpeciesShortResponse> getLineages() {
        return speciesRepository.findAllByParentIsNotNull().stream()
                .filter(species -> !species.isHiddenEntity())
                .map(speciesMapper::toShort)
                .toList();
    }

    public Collection<SpeciesShortResponse> getAllLineages(String url) {
        var sources = sourceSavedFilterService.getSavedSources();

        Species species = speciesRepository.findById(url)
                .orElseThrow(() -> new EntityNotFoundException("Вид не найден URL: " + url));
        return species.getLineages().stream()
            .filter(lineages -> sources.contains(lineages.getSource().getAcronym()))
            .map(speciesMapper::toShort)
            .toList();
    }

    public SpeciesDetailResponse addParent(String speciesUrl, String speciesParentUrl) {
        Species species = findByUrl(speciesUrl);
        Species parent = findByUrl(speciesParentUrl);
        //на этапе save, мы делаем ссылку на самого себя, если это родитель
        //тут же мы проверяем это утверждение.
        if (parent.getParent().equals(parent)) {
            species.setParent(parent);

            Optional.ofNullable(parent.getLineages())
                    .orElseGet(() -> {
                        parent.setLineages(new ArrayList<>());
                        return parent.getLineages();
                    })
                    .add(species);

            Species saved = speciesRepository.save(species);
            SpeciesDetailResponse response = speciesMapper.toDetail(saved);
            applyComputedDarkVision(response, saved);
            return response;
        } else {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "This is not a parent Species");
        }
    }

    @Transactional
    public String update(String oldUrl, SpeciesRequest request) {
        Species existing = speciesRepository.findById(oldUrl)
                .orElse(null);
        if (existing == null) {
            throw new EntityNotFoundException("Species with URL " + oldUrl + " does not exist.");
        }

        if (oldUrl.equals(request.getUrl())) {
            speciesMapper.updateEntity(request, existing);
            if (StringUtils.hasText(request.getParent())) {
                existing.setParent(findByUrl(request.getParent()));
            } else {
                existing.setParent(null);
            }
            existing.setSource(sourceService.findByUrl(request.getSource().getUrl()));
            String url = speciesRepository.save(existing).getUrl();
            syncInnateSpells(url, existing.getParent() != null, request.getInnateSpells());
            revisionService.record(REVISION_ENTITY_TYPE, url, RevisionOperation.UPDATE, findFormByUrl(url));
            return url;
        }

        if (speciesRepository.existsById(request.getUrl())) {
            throw new EntityExistException(String.format("Вид с url %s уже существует", request.getUrl()));
        }
        speciesRepository.deleteSpeciesInnateSpells(oldUrl);
        speciesRepository.deleteLineageInnateSpells(oldUrl);
        speciesRepository.deleteById(oldUrl);
        speciesRepository.flush();
        String url = saveSpecies(request).getUrl();
        revisionService.record(REVISION_ENTITY_TYPE, url, RevisionOperation.UPDATE, findFormByUrl(url));
        return url;
    }

    public SpeciesDetailResponse addSubSpecies(String speciesUrl, List<String> lineagesUrls) {
        Species species = findByUrl(speciesUrl);

        List<Species> subSpeciesEntities = lineagesUrls.stream()
                .map(url -> {
                    Species subSpecies = findByUrl(url);
                    subSpecies.setParent(species);
                    return subSpecies;
                })
                .toList();

        species.setLineages(subSpeciesEntities);
        Species saved = speciesRepository.save(species);
        SpeciesDetailResponse response = speciesMapper.toDetail(saved);
        applyComputedDarkVision(response, saved);
        return response;
    }

    @Transactional(readOnly = true)
    public SpeciesRequest findFormByUrl(final String url) {
        Species species = findByUrl(url);
        SpeciesRequest request = speciesMapper.toRequest(species);
        request.setInnateSpells(loadInnateSpellRequests(url));
        return request;
    }

    // Private methods
    private Species findByUrl(String url) {
        return speciesRepository.findById(url)
                .orElseThrow(() -> new EntityNotFoundException("Species not found with URL: " + url));
    }

    private SpeciesDetailResponse saveSpecies(SpeciesRequest request) {
        Species species = speciesMapper.toEntity(request);
        if (StringUtils.hasText(request.getParent())) {
            var parent = findByUrl(request.getParent());
            species.setParent(parent);
        }
        var source = sourceService.findByUrl(request.getSource().getUrl());

        species.setSource(source);

        Species save = speciesRepository.save(species);
        syncInnateSpells(save.getUrl(), save.getParent() != null, request.getInnateSpells());
        return speciesMapper.toDetail(save);
    }

    public SpeciesDetailResponse preview(final SpeciesRequest request) {
        var source = sourceService.findByUrl(request.getSource().getUrl());
        var species = speciesMapper.toEntity(request);
        species.setSource(source);
        // Родитель нужен предпросмотру происхождения: без него вычисленное
        // тёмное зрение разошлось бы с тем, что покажет сохранённая запись
        if (StringUtils.hasText(request.getParent())) {
            species.setParent(findByUrl(request.getParent()));
        }
        SpeciesDetailResponse response = speciesMapper.toDetail(species);
        // Заклинания умений — оттуда же, откуда их берёт сохранённый вид: иначе
        // предпросмотр показывал бы вид без них
        response.setInnateSpells(mergeInnateSpells(
                resolveInnateSpells(existingSpellsOnly(featureInnateSpellRequests(species))),
                resolveInnateSpells(request.getInnateSpells())
        ));
        applyComputedDarkVision(response, species);
        resolveSpellLists(response);
        return response;
    }

    private SpeciesDetailResponse toDetailWithInnateSpells(Species species)
    {
        SpeciesDetailResponse response = speciesMapper.toDetail(species);
        response.setInnateSpells(innateSpellsOf(species));
        applyComputedDarkVision(response, species);
        resolveSpellLists(response);
        return response;
    }

    /**
     * Врождённые заклинания вида: то, что дают его умения, плюс связанное с самим видом.
     *
     * <p>Два источника, потому что заклинания переехали к умениям, а записи, сохранённые
     * до этого, остались в связующей таблице. Потребителю — листу персонажа и странице
     * вида — разница не видна: он получает один список, как и раньше.</p>
     */
    private List<SpeciesInnateSpellResponse> innateSpellsOf(Species species)
    {
        return mergeInnateSpells(
                resolveInnateSpells(existingSpellsOnly(featureInnateSpellRequests(species))),
                loadInnateSpells(species.getUrl())
        );
    }

    /**
     * Заклинания умений вида. Уровень доступа берётся у самого умения — своего поля у
     * ссылки нет, и второе место для одного и того же расходилось бы с первым.
     */
    private List<SpeciesInnateSpellRequest> featureInnateSpellRequests(Species species)
    {
        if (CollectionUtils.isEmpty(species.getFeatures()))
        {
            return List.of();
        }

        List<SpeciesInnateSpellRequest> requests = new ArrayList<>();

        for (SpeciesFeature feature : species.getFeatures())
        {
            if (CollectionUtils.isEmpty(feature.getGrantedSpells()))
            {
                continue;
            }

            for (GrantedSpellRef ref : feature.getGrantedSpells())
            {
                if (!StringUtils.hasText(ref.getUrl()))
                {
                    continue;
                }

                SpeciesInnateSpellRequest request = new SpeciesInnateSpellRequest();

                request.setSpell(ref.getUrl());
                request.setRequiredLevel(Optional.ofNullable(ref.getRequiredLevel())
                        .orElseGet(() -> Optional.ofNullable(feature.getLevel()).orElse(1)));
                requests.add(request);
            }
        }

        return requests;
    }

    /**
     * Отбрасывает ссылки на заклинания, которых в справочнике уже нет.
     *
     * <p>У связующей таблицы такого не бывает — там внешний ключ. Ссылка умения лежит в
     * jsonb, и удаление заклинания её не убирает: без проверки страница вида падала бы
     * целиком из-за одной устаревшей ссылки.</p>
     */
    private List<SpeciesInnateSpellRequest> existingSpellsOnly(List<SpeciesInnateSpellRequest> requests)
    {
        if (requests.isEmpty())
        {
            return List.of();
        }

        Set<String> urls = requests.stream()
                .map(SpeciesInnateSpellRequest::getSpell)
                .collect(Collectors.toSet());
        Set<String> known = spellRepository.findAllShortByUrlIn(urls).stream()
                .map(Spell::getUrl)
                .collect(Collectors.toSet());

        return requests.stream()
                .filter(request -> known.contains(request.getSpell()))
                .toList();
    }

    private List<SpeciesInnateSpellResponse> loadInnateSpells(String speciesUrl)
    {
        return resolveInnateSpells(loadInnateSpellRequests(speciesUrl));
    }

    private List<SpeciesInnateSpellRequest> loadInnateSpellRequests(String speciesUrl)
    {
        return speciesRepository.findInnateSpells(speciesUrl).stream()
                .map(this::toInnateSpellRequest)
                .toList();
    }

    private SpeciesInnateSpellRequest toInnateSpellRequest(SpeciesInnateSpellView view)
    {
        SpeciesInnateSpellRequest request = new SpeciesInnateSpellRequest();
        request.setSpell(view.getSpellUrl());
        request.setRequiredLevel(view.getRequiredLevel());
        return request;
    }

    private List<SpeciesInnateSpellResponse> resolveInnateSpells(Collection<SpeciesInnateSpellRequest> requests)
    {
        if (requests == null || requests.isEmpty())
        {
            return List.of();
        }

        Map<String, Spell> spellsByUrl = spellRepository.findAllShortByUrlIn(
                        requests.stream().map(SpeciesInnateSpellRequest::getSpell).toList())
                .stream()
                .collect(Collectors.toMap(Spell::getUrl, spell -> spell));

        return requests.stream()
                .map(request -> {
                    Spell spell = Optional.ofNullable(spellsByUrl.get(request.getSpell()))
                            .orElseThrow(() -> new EntityNotFoundException("Spell not found with URL: " + request.getSpell()));
                    SpeciesInnateSpellResponse response = new SpeciesInnateSpellResponse();
                    response.setSpell(spellMapper.toShort(spell));
                    response.setRequiredLevel(validateRequiredLevel(request.getRequiredLevel()));
                    return response;
                })
                .toList();
    }

    private List<SpeciesInnateSpellResponse> mergeInnateSpells(
            Collection<SpeciesInnateSpellResponse> parentSpells,
            Collection<SpeciesInnateSpellResponse> lineageSpells)
    {
        Map<String, SpeciesInnateSpellResponse> spellsByUrl = new LinkedHashMap<>();
        parentSpells.forEach(spell -> spellsByUrl.put(spell.getSpell().getUrl(), spell));
        lineageSpells.forEach(spell -> spellsByUrl.merge(
                spell.getSpell().getUrl(),
                spell,
                (parentSpell, lineageSpell) -> lineageSpell.getRequiredLevel() <= parentSpell.getRequiredLevel()
                        ? lineageSpell
                        : parentSpell
        ));
        return List.copyOf(spellsByUrl.values());
    }

    private void syncInnateSpells(
            String speciesUrl,
            boolean lineage,
            Collection<SpeciesInnateSpellRequest> requests)
    {
        speciesRepository.deleteSpeciesInnateSpells(speciesUrl);
        speciesRepository.deleteLineageInnateSpells(speciesUrl);

        if (requests == null || requests.isEmpty())
        {
            return;
        }

        Set<String> spellUrls = requests.stream()
                .map(SpeciesInnateSpellRequest::getSpell)
                .collect(Collectors.toSet());
        if (spellUrls.size() != requests.size())
        {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Врождённое заклинание указано несколько раз");
        }
        if (spellRepository.countByUrlIn(spellUrls) != spellUrls.size())
        {
            throw new EntityNotFoundException("Одно или несколько врождённых заклинаний не найдены");
        }

        requests.forEach(request -> {
            Integer requiredLevel = validateRequiredLevel(request.getRequiredLevel());
            if (lineage)
            {
                speciesRepository.addLineageInnateSpell(speciesUrl, request.getSpell(), requiredLevel);
            }
            else
            {
                speciesRepository.addSpeciesInnateSpell(speciesUrl, request.getSpell(), requiredLevel);
            }
        });
    }

    private Integer validateRequiredLevel(Integer requiredLevel)
    {
        if (requiredLevel == null || requiredLevel < 1 || requiredLevel > 20)
        {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Уровень врождённого заклинания должен быть от 1 до 20");
        }
        return requiredLevel;
    }

    /**
     * Проставляет детали вычисленное тёмное зрение — наибольшую дальность чувства
     * {@code DARKVISION} в механике записи и её умений; у происхождения учитывается и
     * родитель. Своего поля у записи больше нет: тёмное зрение дарит умение, а статблок
     * и лист персонажа продолжают читать {@code properties.darkVision} как раньше.
     */
    private void applyComputedDarkVision(SpeciesDetailResponse response, Species species)
    {
        if (response.getProperties() == null)
        {
            return;
        }
        Integer own = darkVisionOf(species);
        Integer inherited = species.getParent() == null ? null : darkVisionOf(species.getParent());
        response.getProperties().setDarkVision(Stream.of(own, inherited)
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(null));
    }

    /** Наибольшая дальность {@code DARKVISION} в механике записи и её умений; {@code null} — нет. */
    private Integer darkVisionOf(Species species)
    {
        Stream<SpeciesMechanics> featureMechanics = CollectionUtils.isEmpty(species.getFeatures())
                ? Stream.empty()
                : species.getFeatures().stream()
                        .filter(Objects::nonNull)
                        .map(SpeciesFeature::getMechanics);
        return Stream.concat(Stream.ofNullable(species.getMechanics()), featureMechanics)
                .filter(Objects::nonNull)
                .map(SpeciesMechanics::getModifiers)
                .filter(Objects::nonNull)
                .map(SheetModifiers::getSenses)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .filter(sense -> sense.getType() == SenseType.DARKVISION)
                .map(SenseGrant::getRange)
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(null);
    }
}
