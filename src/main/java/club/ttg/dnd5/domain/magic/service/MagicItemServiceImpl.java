package club.ttg.dnd5.domain.magic.service;

import club.ttg.dnd5.domain.magic.repository.MagicItemRepository;
import club.ttg.dnd5.domain.magic.rest.dto.MagicItemDetailResponse;
import club.ttg.dnd5.domain.magic.rest.dto.MagicItemRequest;
import club.ttg.dnd5.domain.magic.rest.dto.MagicItemShortResponse;
import club.ttg.dnd5.domain.magic.rest.mapper.MagicItemMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
@Service
public class MagicItemServiceImpl implements MagicItemService {
    private final MagicItemRepository magicItemRepository;
    private final MagicItemMapper magicItemMapper;

    @Override
    public boolean existsByUrl(String url) {
        return false;
    }

    @Override
    public MagicItemDetailResponse getItem(String url) {
        return null;
    }

    @Override
    public Collection<MagicItemShortResponse> getItems() {
        return List.of();
    }

    @Override
    public String addItem(MagicItemRequest itemDto) {
        return "";
    }

    @Override
    public String updateItem(String url, MagicItemRequest itemDto) {
        return "";
    }

    @Override
    public String delete(String itemUrl) {
        return "";
    }
}
