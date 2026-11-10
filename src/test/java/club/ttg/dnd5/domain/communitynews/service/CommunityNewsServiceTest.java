package club.ttg.dnd5.domain.communitynews.service;

import club.ttg.dnd5.domain.communitynews.model.CommunityNews;
import club.ttg.dnd5.domain.communitynews.model.CommunityNewsStatus;
import club.ttg.dnd5.domain.communitynews.repository.CommunityNewsRepository;
import club.ttg.dnd5.domain.communitynews.rest.dto.CommunityNewsDetailedResponse;
import club.ttg.dnd5.domain.communitynews.rest.dto.CommunityNewsRequest;
import club.ttg.dnd5.domain.communitynews.rest.mapper.CommunityNewsMapperImpl;
import club.ttg.dnd5.exception.EntityExistException;
import club.ttg.dnd5.exception.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Limit;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CommunityNewsServiceTest {

    private CommunityNewsRepository repository;
    private CommunityNewsService service;

    @BeforeEach
    void setUp() {
        repository = mock(CommunityNewsRepository.class);
        when(repository.save(any(CommunityNews.class))).thenAnswer(invocation -> invocation.getArgument(0));
        service = new CommunityNewsService(repository, new CommunityNewsMapperImpl());
    }

    private static CommunityNewsRequest request(String url) {
        CommunityNewsRequest request = new CommunityNewsRequest();
        request.setUrl(url);
        request.setTitle("Новость");
        request.setAuthorName("");
        request.setSourceUrl("");
        request.setVideoUrl("  ");
        request.setPreview("[\"анонс\"]");
        request.setContent(" \n ");
        return request;
    }

    private CommunityNews saved() {
        ArgumentCaptor<CommunityNews> captor = ArgumentCaptor.forClass(CommunityNews.class);
        verify(repository).save(captor.capture());
        return captor.getValue();
    }

    @Test
    void draftIsNotActiveAndKeepsPlannedDate() {
        Instant planned = Instant.now().plus(Duration.ofDays(3));
        CommunityNewsRequest request = request("draft");
        request.setDraft(true);
        request.setActive(true);
        request.setPublishDateTime(planned);

        assertEquals("draft", service.save(request));

        CommunityNews news = saved();
        assertTrue(news.isDraft());
        assertFalse(news.isActive());
        assertEquals(planned, news.getPublishDateTime());
        assertEquals(CommunityNewsStatus.DRAFT, news.getStatus());
        assertFalse(news.isDeleted());
    }

    @Test
    void publishedWithoutDateGoesLiveNow() {
        CommunityNewsRequest request = request("live");
        request.setActive(true);

        service.save(request);

        CommunityNews news = saved();
        assertNotNull(news.getPublishDateTime());
        assertEquals(CommunityNewsStatus.ACTIVE, news.getStatus());
    }

    @Test
    void futureDateIsScheduled() {
        CommunityNewsRequest request = request("later");
        request.setActive(true);
        request.setPublishDateTime(Instant.now().plus(Duration.ofHours(2)));

        service.save(request);

        assertEquals(CommunityNewsStatus.SCHEDULED, saved().getStatus());
    }

    @Test
    void inactiveKeepsExistingDateAndLinkAccess() {
        Instant published = Instant.now().minus(Duration.ofDays(1));
        CommunityNews existing = new CommunityNews();
        existing.setUrl("old");
        existing.setPublishDateTime(published);
        existing.setActive(true);
        when(repository.findByUrl("old")).thenReturn(Optional.of(existing));

        CommunityNewsRequest request = request("old");
        request.setActive(false);
        request.setAccessibleByLink(true);
        service.update("old", request);

        assertFalse(existing.isActive());
        assertTrue(existing.isAccessibleByLink());
        assertEquals(published, existing.getPublishDateTime());
        assertEquals(CommunityNewsStatus.INACTIVE, existing.getStatus());
    }

    @Test
    void blankOptionalFieldsAreStoredAsNull() {
        CommunityNewsRequest request = request("blank");
        request.setActive(true);

        service.save(request);

        CommunityNews news = saved();
        assertNull(news.getAuthorName());
        assertNull(news.getSourceUrl());
        assertNull(news.getVideoUrl());
        assertNull(news.getContent());
    }

    @Test
    void rawFormReturnsEmptyStringsInsteadOfNull() {
        CommunityNews existing = new CommunityNews();
        existing.setUrl("raw");
        existing.setTitle("Новость");
        when(repository.findByUrl("raw")).thenReturn(Optional.of(existing));

        CommunityNewsRequest form = service.findFormByUrl("raw");

        assertEquals("", form.getAuthorName());
        assertEquals("", form.getPreview());
        assertEquals("", form.getContent());
    }

    @Test
    void takenUrlIsRejectedOnCreateAndRename() {
        when(repository.existsByUrl("taken")).thenReturn(true);
        assertThrows(EntityExistException.class, () -> service.save(request("taken")));

        CommunityNews existing = new CommunityNews();
        existing.setUrl("mine");
        when(repository.findByUrl("mine")).thenReturn(Optional.of(existing));
        assertThrows(EntityExistException.class, () -> service.update("mine", request("taken")));
        verify(repository, never()).save(any());
    }

    @Test
    void keepingOwnUrlOnUpdateIsAllowed() {
        CommunityNews existing = new CommunityNews();
        existing.setUrl("mine");
        when(repository.findByUrl("mine")).thenReturn(Optional.of(existing));
        when(repository.existsByUrl("mine")).thenReturn(true);

        assertEquals("mine", service.update("mine", request("mine")));
    }

    @Test
    void urlExistenceCheckAnswersByAnyRecord() {
        when(repository.existsByUrl("busy")).thenReturn(true);
        service.validateUrlExistence("busy");
        assertThrows(EntityNotFoundException.class, () -> service.validateUrlExistence("free"));
    }

    @Test
    void deleteIsSoft() {
        CommunityNews existing = new CommunityNews();
        existing.setUrl("gone");
        when(repository.findByUrl("gone")).thenReturn(Optional.of(existing));

        assertEquals("gone", service.delete("gone"));
        assertTrue(saved().isDeleted());
    }

    @Test
    void publicReadGoesThroughAccessibilityQuery() {
        CommunityNews news = new CommunityNews();
        news.setUrl("open");
        news.setTitle("Новость");
        news.setAccessibleByLink(true);
        when(repository.findAccessibleByUrl(eq("open"), any())).thenReturn(Optional.of(news));
        when(repository.findAccessibleByUrl(eq("hidden"), any())).thenReturn(Optional.empty());

        CommunityNewsDetailedResponse response = service.findByUrl("open");
        assertEquals(CommunityNewsStatus.INACTIVE, response.getStatus());
        assertEquals("Неактивна", response.getStatusName());
        assertThrows(EntityNotFoundException.class, () -> service.findByUrl("hidden"));
    }

    @Test
    void searchDefaultsToTenAndCaseInsensitivePattern() {
        when(repository.findPublished(any(), anyString(), any())).thenReturn(List.of());

        service.searchPublished(null, "  ДрАкон ");
        verify(repository).findPublished(any(), eq("%дракон%"), eq(Limit.of(10)));

        service.searchPublished(5, null);
        verify(repository).findPublished(any(), eq("%"), eq(Limit.of(5)));
    }

    @Test
    void previewDoesNotSave() {
        CommunityNewsRequest request = request("preview");
        request.setActive(true);
        request.setVideoUrl("https://youtu.be/x");

        CommunityNewsDetailedResponse response = service.preview(request);

        assertEquals("https://youtu.be/x", response.getVideoUrl());
        assertEquals(CommunityNewsStatus.ACTIVE, response.getStatus());
        verify(repository, never()).save(any());
    }
}
