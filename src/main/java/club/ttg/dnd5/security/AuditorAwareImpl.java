package club.ttg.dnd5.security;

import club.ttg.dnd5.domain.user.model.User;
import io.micrometer.common.lang.NonNullApi;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.util.Optional;

@NonNullApi
@Component
public class AuditorAwareImpl implements AuditorAware<String> {
    @Override
    public Optional<String> getCurrentAuditor() {
        try {
            User user = SecurityUtils.getUser();

            return Optional.ofNullable(user.getUsername());
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
