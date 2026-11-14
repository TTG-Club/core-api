package club.ttg.dnd5.domain.source.service;

import club.ttg.dnd5.domain.source.model.Source;
import club.ttg.dnd5.domain.source.repository.SourceRepository;
import club.ttg.dnd5.domain.source.rest.dto.SourceRequest;
import club.ttg.dnd5.domain.source.rest.mapper.SourceMapper;
import club.ttg.dnd5.exception.ApiException;
import club.ttg.dnd5.exception.EntityExistException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class SourceServiceTest {
    private static final String URL = "xanathars-guide";

    private final SourceRepository repository = mock(SourceRepository.class);
    private final SourceMapper mapper = mock(SourceMapper.class);
    private final SourceAcronymRenamer renamer = mock(SourceAcronymRenamer.class);
    private final SourceService service = new SourceService(repository, mapper, renamer);

    /** Акроним не менялся — обычное сохранение, без переименования. */
    @Test
    void sameAcronymIsPlainUpdate() {
        Source source = givenSource("XGE");

        assertEquals(URL, service.update(URL, request("XGE")));

        verifyNoInteractions(renamer);
        verify(mapper).toEntity(any(SourceRequest.class), eq(source));
        verify(repository).save(source);
    }

    /** Другой акроним — сначала переименование, потом остальные поля на перечитанный источник. */
    @Test
    void differentAcronymRenamesThenUpdates() {
        Source before = source("XGE");
        Source after = source("XGTE");
        when(repository.findByUrl(URL)).thenReturn(Optional.of(before), Optional.of(after));
        when(repository.save(after)).thenReturn(after);
        SourceRequest request = request(" XGTE ");

        assertEquals(URL, service.update(URL, request));

        var order = inOrder(renamer, mapper, repository);
        order.verify(renamer).rename(before, "XGTE", null);
        order.verify(mapper).toEntity(request, after);
        order.verify(repository).save(after);
        assertEquals("XGTE", request.getAcronym());
    }

    /** Смена одного лишь регистра — обычное переименование, а не «уже существует». */
    @Test
    void caseOnlyChangeIsRename() {
        Source before = source("phb");
        Source after = source("PHB");
        when(repository.findByUrl(URL)).thenReturn(Optional.of(before), Optional.of(after));
        when(repository.existsById("PHB")).thenReturn(false);
        when(repository.save(after)).thenReturn(after);

        service.update(URL, request("PHB"));

        verify(renamer).rename(before, "PHB", null);
        verify(repository).existsByVttgKeyAndAcronymNot("phb", "phb");
    }

    /** Акроним занят другой книгой — отказ, ничего не меняется. */
    @Test
    void takenAcronymIsRejected() {
        givenSource("XGE");
        when(repository.existsById("PHB")).thenReturn(true);

        EntityExistException failure = assertThrows(EntityExistException.class,
                () -> service.update(URL, request("PHB")));

        assertEquals("Книга с таким акронимом уже существует", failure.getMessage());
        verifyNoInteractions(renamer, mapper);
        verify(repository, never()).save(any());
    }

    /** Под акронимом в VTTG числится другая, переименованная книга — занять его нельзя. */
    @Test
    void acronymFrozenInVttgForAnotherSourceIsRejected() {
        givenSource("TCE");
        when(repository.existsByVttgKeyAndAcronymNot("xge", "TCE")).thenReturn(true);

        assertThrows(EntityExistException.class, () -> service.update(URL, request("XGE")));

        verifyNoInteractions(renamer, mapper);
        verify(repository, never()).save(any());
    }

    /** Пустой акроним или одни пробелы — 400, ничего не меняется. */
    @Test
    void blankAcronymIsBadRequest() {
        givenSource("XGE");

        for (String blank : new String[]{null, "", "   "}) {
            ApiException failure = assertThrows(ApiException.class, () -> service.update(URL, request(blank)));
            assertEquals(HttpStatus.BAD_REQUEST, failure.getStatus());
        }

        verifyNoInteractions(renamer, mapper);
        verify(repository, never()).save(any());
        verify(repository, never()).existsById(anyString());
    }

    /** Сбой посреди переименования уходит наружу — транзакция откатывается целиком. */
    @Test
    void renameFailurePropagates() {
        Source source = givenSource("XGE");
        doThrow(new IllegalStateException("boom")).when(renamer).rename(source, "XGTE", null);

        assertThrows(IllegalStateException.class, () -> service.update(URL, request("XGTE")));

        verifyNoInteractions(mapper);
        verify(repository, never()).save(any());
    }

    private Source givenSource(String acronym) {
        Source source = source(acronym);
        when(repository.findByUrl(URL)).thenReturn(Optional.of(source));
        when(repository.save(source)).thenReturn(source);
        return source;
    }

    private static Source source(String acronym) {
        Source source = new Source();
        source.setAcronym(acronym);
        source.setUrl(URL);
        return source;
    }

    private static SourceRequest request(String acronym) {
        SourceRequest request = new SourceRequest();
        request.setAcronym(acronym);
        request.setUrl(URL);
        return request;
    }
}
