package club.ttg.dnd5.domain.user.service;

import club.ttg.dnd5.domain.common.service.RatingService;
import club.ttg.dnd5.domain.user.model.User;
import club.ttg.dnd5.domain.user.rest.dto.UserDto;
import club.ttg.dnd5.domain.user.rest.dto.UserProfileShortResponse;
import club.ttg.dnd5.dto.base.KeyValueDto;
import club.ttg.dnd5.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService
{
    private final RatingService ratingService;

    public Optional<UUID> getCurrentUserId()
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.getPrincipal() instanceof User user)
        {
            return Optional.ofNullable(user.getUuid());
        }
        return Optional.empty();
    }

    public UserProfileShortResponse getUserProfileShort()
    {
        UserDto userDto = SecurityUtils.getUserDto();
        UserProfileShortResponse profileShortResponse = UserProfileShortResponse.builder()
                .username(userDto.getUsername())
                .email(userDto.getEmail())
                .build();

        profileShortResponse.setStatistics(getUserStatistics(userDto.getUsername()));

        return profileShortResponse;
    }

    private List<KeyValueDto> getUserStatistics(String username)
    {
        Long userRatingCount = ratingService.countUserRating(username);

        return List.of(KeyValueDto.builder()
                .key("Количество оценок")
                .value(userRatingCount)
                .build());
    }

}
