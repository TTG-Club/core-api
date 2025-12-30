package club.ttg.dnd5.domain.user.service;

import club.ttg.dnd5.domain.common.service.RatingService;
import club.ttg.dnd5.domain.user.model.User;
import club.ttg.dnd5.domain.user.repository.UserRepository;
import club.ttg.dnd5.domain.user.rest.dto.UserDto;
import club.ttg.dnd5.domain.user.rest.dto.UserProfileDetailedResponse;
import club.ttg.dnd5.domain.user.rest.dto.UserStatisticsDto;
import club.ttg.dnd5.exception.ApiException;
import club.ttg.dnd5.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final RatingService ratingService;
    private final UserRepository userRepository;

    public UserProfileDetailedResponse getUserProfileDetailed() {
        UserDto currentUser = SecurityUtils.getUserDto();
        Long userRatingCount = ratingService.countUserRating(currentUser.getUsername());
        return UserProfileDetailedResponse.builder()
                .user(currentUser)
                .statistics(UserStatisticsDto.builder()
                        .ratingCount(userRatingCount)
                        .build())
                .build();
    }

    public User getByUsername(String username) throws ApiException {
        return userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new ApiException(HttpStatus.UNAUTHORIZED, "Пользователь не найден"));
    }

    public User getByUsernameOrEmail(String usernameOrEmail) throws ApiException {
        return userRepository.findByEmailOrUsername(usernameOrEmail)
                .orElseThrow(() ->
                        new ApiException(HttpStatus.UNAUTHORIZED, "Пользователь не найден"));
    }

    public Optional<UUID> getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.getPrincipal() instanceof User user) {
            return Optional.of(user.getUuid());
        }
        return Optional.empty();
    }

    public UserDetailsService userDetailsService() {
        return this::getByUsernameOrEmailForSecurity;
    }

    public UserDetails getByUsernameOrEmailForSecurity(String usernameOrEmail) {
        User user = getByUsernameOrEmail(usernameOrEmail);

        return org.springframework.security.core.userdetails.User.withUsername(user.getUsername())
                .password(user.getPassword())
                .build();
    }
}
