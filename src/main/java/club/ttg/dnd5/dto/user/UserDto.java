package club.ttg.dnd5.dto.user;

import lombok.*;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private String username;
    private String email;
    private List<String> roles;
    private Instant createdAt;
    private Instant updatedAt;
}
