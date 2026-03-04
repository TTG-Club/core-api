package club.ttg.dnd5.domain.user.service;

import club.ttg.dnd5.domain.common.service.RatingService;
import club.ttg.dnd5.domain.user.model.User;
import club.ttg.dnd5.domain.user.repository.RoleRepository;
import club.ttg.dnd5.domain.user.repository.UserRepository;
import club.ttg.dnd5.domain.user.rest.dto.UserDto;
import club.ttg.dnd5.domain.user.rest.dto.UserProfileShortResponse;
import club.ttg.dnd5.domain.user.rest.mapper.UserMapper;
import club.ttg.dnd5.dto.base.KeyValueDto;
import club.ttg.dnd5.exception.ApiException;
import club.ttg.dnd5.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService
{
    private final RatingService ratingService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;

    public Collection<UserDto> getUsers(String search)
    {
        return userRepository.findAll(
                        UserSpecifications.nicknameOrEmailContains(search),
                        Sort.by("username")
                ).stream()
                .map(userMapper::toDto)
                .toList();
    }

    public User getByUsername(String username) throws ApiException
    {
        return userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new ApiException(HttpStatus.UNAUTHORIZED, "Пользователь не найден"));
    }

    public User getByUsernameOrEmail(String usernameOrEmail) throws ApiException
    {
        return userRepository.findByEmailOrUsername(usernameOrEmail)
                .orElseThrow(() ->
                        new ApiException(HttpStatus.UNAUTHORIZED, "Пользователь не найден"));
    }

    public Optional<UUID> getCurrentUserId()
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.getPrincipal() instanceof User user)
        {
            return Optional.of(user.getUuid());
        }
        return Optional.empty();
    }

    public UserDetailsService userDetailsService()
    {
        return this::getByUsernameOrEmailForSecurity;
    }

    public UserDetails getByUsernameOrEmailForSecurity(String usernameOrEmail)
    {
        User user = getByUsernameOrEmail(usernameOrEmail);

        return org.springframework.security.core.userdetails.User.withUsername(user.getUsername())
                .password(user.getPassword())
                .build();
    }

    public UserProfileShortResponse getUserProfileShort()
    {
        UserDto userDto = SecurityUtils.getUserDto();
        User user = userRepository.findByUsername(userDto.getUsername())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Пользователь не авторизован"));
        UserProfileShortResponse profileShortResponse = userMapper.toProfileShortResponse(user);

        profileShortResponse.setStatistics(getUserStatistics(user.getUsername()));

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

    public long count()
    {
        return userRepository.count();
    }

    /**
     * Изменить роли пользователя.
     */
    @Transactional
    public UserDto updateUserRoles(UUID userId, Set<String> roles)
    {
        if (userId == null)
        {
            throw new ApiException(HttpStatus.BAD_REQUEST, "userId обязателен");
        }
        if (roles == null)
        {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Список ролей обязателен");
        }

        User currentUser = SecurityUtils.getUser();
        if (currentUser == null)
        {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Пользователь не авторизован");
        }

        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> "ADMIN".equals(a.getAuthority()));
        if (!isAdmin)
        {
            throw new ApiException(HttpStatus.FORBIDDEN, "Недостаточно прав для изменения ролей");
        }

        Set<String> normalizedRoles = roles.stream()
                .filter(r -> r != null && !r.isBlank())
                .map(String::trim)
                .collect(java.util.stream.Collectors.toUnmodifiableSet());

        if (normalizedRoles.isEmpty())
        {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Список ролей не должен быть пустым");
        }

        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Пользователь не найден"));

        targetUser.setRoles(roleRepository.findAllByNameIn(normalizedRoles));
        User saved = userRepository.save(targetUser);
        return userMapper.toDto(saved);
    }
}