package club.ttg.dnd5.domain.user.model;

import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
public class UserPreferences {
    private String themeName = "dark";
    private Boolean disableDrawerInSection = false;
    private Boolean disableLinksInNewTab = false;
}
