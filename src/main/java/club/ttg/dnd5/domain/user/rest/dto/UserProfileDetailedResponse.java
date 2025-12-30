package club.ttg.dnd5.domain.user.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDetailedResponse {

    @Schema(description = "Информация о пользователе")
    private UserDto user;

    @Schema(description = "Статистика пользователя")
    private UserStatisticsDto statistics;
}
