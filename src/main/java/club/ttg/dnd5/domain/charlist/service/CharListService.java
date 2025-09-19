package club.ttg.dnd5.domain.charlist.service;

import club.ttg.dnd5.domain.charlist.repository.CharListRepository;
import club.ttg.dnd5.domain.charlist.rest.dto.CharListDetailedResponse;
import club.ttg.dnd5.domain.charlist.rest.dto.CharListRequest;
import club.ttg.dnd5.domain.charlist.rest.dto.CharListShortResponse;
import club.ttg.dnd5.domain.charlist.rest.mapper.CharListMapper;
import club.ttg.dnd5.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;

@RequiredArgsConstructor
@Service
public class CharListService {
    private final CharListRepository charListRepository;
    private final CharListMapper charListMapper;

    public Collection<CharListShortResponse> getAllByUser(final String username) {
        return charListRepository.findByCreatedBy(username).stream()
                .map(charListMapper::toShort)
                .toList();
    }

    public long countCurrentCharList(final String username) {
        return charListRepository.countByCreatedBy(username);
    }

    public String save(final CharListRequest request) {
        return charListRepository.save(charListMapper.toEntity(request)).getId();
    }

    public String update(final CharListRequest request) {
        return null;
    }

    public CharListDetailedResponse getById(final String id) {
        return charListRepository.findById(id).map(charListMapper::toDetailed)
                .orElseThrow(() -> new EntityNotFoundException("Лист персонажа не найден"));
    }
}