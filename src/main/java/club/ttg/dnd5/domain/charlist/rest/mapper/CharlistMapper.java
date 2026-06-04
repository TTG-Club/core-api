package club.ttg.dnd5.domain.charlist.rest.mapper;

import club.ttg.dnd5.domain.charlist.model.Charlist;
import club.ttg.dnd5.domain.charlist.rest.dto.CharlistResponse;
import club.ttg.dnd5.domain.charlist.rest.dto.CharlistShortResponse;
import org.springframework.stereotype.Component;

@Component
public class CharlistMapper {

    public CharlistResponse toResponse(Charlist entity) {
        return CharlistResponse.builder()
                .id(entity.getId())
                .characterName(entity.getCharacterName())
                .characterLevel(entity.getCharacterLevel())
                .characterClass(entity.getCharacterClass())
                .data(entity.getData())
                .visibility(entity.getVisibility())
                .shareToken(entity.getShareToken())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public CharlistShortResponse toShortResponse(Charlist entity) {
        return CharlistShortResponse.builder()
                .id(entity.getId())
                .characterName(entity.getCharacterName())
                .characterLevel(entity.getCharacterLevel())
                .characterClass(entity.getCharacterClass())
                .visibility(entity.getVisibility())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
