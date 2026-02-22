package club.ttg.dnd5.domain.source.service;

import club.ttg.dnd5.domain.filter.model.FilterInfo;
import club.ttg.dnd5.domain.filter.service.AbstractSavedFilterService;
import club.ttg.dnd5.domain.source.model.Source;
import club.ttg.dnd5.domain.source.model.SourceType;
import club.ttg.dnd5.domain.source.model.filter.SourceSavedFilter;
import club.ttg.dnd5.domain.source.repository.SourceSavedFilterRepository;
import club.ttg.dnd5.domain.source.rest.dto.filter.SourceGroupFilter;
import club.ttg.dnd5.domain.user.service.UserService;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SourceSavedFilterService extends AbstractSavedFilterService<SourceSavedFilter> {

    private final SourceService sourceService;

    private static final String FILTER_VERSION = "1.0";

    public SourceSavedFilterService(SourceSavedFilterRepository repository, UserService userService, SourceService sourceService) {
        super(repository, userService);
        this.sourceService = sourceService;
    }

    @Override
    protected FilterInfo buildDefaultFilterInfo() {
        Map<SourceType, List<Source>> sourceMap = sourceService.findAll().stream()
                .collect(Collectors.groupingBy(Source::getType));

        return new FilterInfo(Arrays.stream(SourceType.values())
                .filter(sourceMap::containsKey)
                .map(type -> new SourceGroupFilter(
                        sourceMap.get(type).stream()
                                .map(src -> new SourceGroupFilter.SpellSourceFilter(src.getName(), src.getAcronym()))
                                .toList(),
                        type.getName()
                )).collect(Collectors.toList()), FILTER_VERSION);
    }
}
