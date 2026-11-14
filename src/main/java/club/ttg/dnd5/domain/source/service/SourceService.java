package club.ttg.dnd5.domain.source.service;

import club.ttg.dnd5.domain.source.model.Source;
import club.ttg.dnd5.domain.source.repository.SourceRepository;
import club.ttg.dnd5.domain.source.rest.dto.PublisherDto;
import club.ttg.dnd5.domain.source.rest.dto.SourceDetailResponse;
import club.ttg.dnd5.domain.source.rest.dto.SourceRequest;
import club.ttg.dnd5.domain.source.rest.dto.SourceShortResponse;
import club.ttg.dnd5.domain.source.rest.mapper.SourceMapper;
import club.ttg.dnd5.domain.user.model.User;
import club.ttg.dnd5.domain.vttg.service.VttgSourceKeys;
import club.ttg.dnd5.exception.ApiException;
import club.ttg.dnd5.exception.EntityExistException;
import club.ttg.dnd5.exception.EntityNotFoundException;
import club.ttg.dnd5.util.SwitchLayoutUtils;
import club.ttg.dnd5.util.YoUtils;
import club.ttg.dnd5.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SourceService {
    private final SourceRepository sourceRepository;
    private final SourceMapper sourceMapper;
    private final SourceAcronymRenamer acronymRenamer;

    private static final String ACRONYM_EXISTS = "Книга с таким акронимом уже существует";

    @Transactional(readOnly = true)
    public List<Source> findAll() {
        return sourceRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<SourceShortResponse> search(String searchLine) {
        if (StringUtils.hasText(searchLine)) {
            var line = YoUtils.replaceYo(searchLine.trim());
            var invertedSearchLine = SwitchLayoutUtils.switchLayout(line);
            return sourceRepository.findBySearchLine(line, invertedSearchLine, Sort.by("name"))
                    .stream()
                    .map(sourceMapper::toShort)
                    .toList();
        }
        return sourceRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(
                        (Source s) -> Optional.ofNullable(s.getPublisher())
                                .map(PublisherDto::getDate)
                                .orElse(null),
                        Comparator.nullsLast(Comparator.naturalOrder())
                ))                .map(sourceMapper::toShort)
                .toList();

    }

    @Transactional(readOnly = true)
    public Source findByUrl(String url) {
        return sourceRepository.findByUrl(url)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Источник с url %s не существует", url))
                );
    }

    @Transactional(readOnly = true)
    public Source findReferenceByUrl(String url) {
        String acronym = sourceRepository.findAcronymByUrl(url)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Источник с url %s не существует", url))
                );
        return sourceRepository.getReferenceById(acronym);
    }

    @Transactional(readOnly = true)
    public SourceDetailResponse findDetailByUrl(String url) {
        return sourceMapper.toDetail(sourceRepository.findByUrl(url)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Источник с акронимом %s не существует", url)))
        );
    }

    public Optional<Source> findByUrOptional(String url) {
        return sourceRepository.findByUrl(url);
    }

    @Transactional
    public String save(final SourceRequest request) {
        if (sourceRepository.existsById(request.getAcronym())) {
            throw new EntityExistException(ACRONYM_EXISTS);
        }
        if (sourceRepository.existsByUrl(request.getUrl())) {
            throw new EntityExistException("Книга с таким url уже существует");
        }
        requireFreeVttgKey(request.getAcronym(), null);

        return sourceRepository.save(sourceMapper.toEntity(request)).getUrl();
    }

    /**
     * Обновляет источник. Если акроним в запросе отличается от сохранённого, источник
     * переименовывается: вместе с ним новый акроним получают весь материал каталога и
     * сохранённые фильтры пользователей ({@link SourceAcronymRenamer}).
     */
    @Transactional
    public String update(String url, final SourceRequest request) {
        var source = sourceRepository.findByUrl(url)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Источник с url %s не существует", url)));
        if (!StringUtils.hasText(request.getAcronym())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Акроним источника не может быть пустым");
        }
        String acronym = request.getAcronym().trim();
        request.setAcronym(acronym);

        if (!acronym.equals(source.getAcronym())) {
            if (sourceRepository.existsById(acronym)) {
                throw new EntityExistException(ACRONYM_EXISTS);
            }
            requireFreeVttgKey(acronym, source.getAcronym());
            User user = SecurityUtils.getUserOrNull();
            acronymRenamer.rename(source, acronym, user == null ? null : user.getUsername());
            // Переименование очистило контекст Hibernate — источник читается заново, уже с новым ключом.
            source = sourceRepository.findByUrl(url)
                    .orElseThrow(() -> new EntityNotFoundException(
                            String.format("Источник с url %s не существует", url)));
        }
        sourceMapper.toEntity(request, source);
        return sourceRepository.save(source).getUrl();
    }

    /**
     * Акроним нельзя занять, если под ним в выгрузке VTTG уже числится другая книга:
     * её ключ заморожен при смене акронима, и новая книга с прежним акронимом получила
     * бы тот же ключ — записи двух книг в компендиуме VTTG перемешались бы.
     *
     * @param acronym       акроним, который хотят занять
     * @param ownAcronym    акроним самого источника (ему свой ключ вернуть можно); {@code null} для нового
     */
    private void requireFreeVttgKey(String acronym, String ownAcronym) {
        String key = VttgSourceKeys.ofAcronym(acronym);
        if (sourceRepository.existsByVttgKeyAndAcronymNot(key, ownAcronym == null ? "" : ownAcronym)) {
            throw new EntityExistException(
                    "Акроним занят: под ним в выгрузке VTTG числится другая книга, у которой акроним сменили");
        }
    }

    @Transactional(readOnly = true)
    public SourceRequest findFormByUrl(final String url) {
        return sourceMapper.toRequest(sourceRepository.findByUrl(url)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Источник с url %s не существует", url))));
    }

    public SourceDetailResponse preview(final SourceRequest request) {
        return sourceMapper.toDetail(sourceMapper.toEntity(request));
    }
}
