package club.ttg.dnd5.domain.common.service.engine;

import club.ttg.dnd5.domain.user.model.OneTimeToken;
import club.ttg.dnd5.domain.user.model.User;
import club.ttg.dnd5.domain.user.repository.OneTimeTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OneTimeTokenService {
    private final OneTimeTokenRepository oneTimeTokenRepository;

    public UUID createOneTimeToken(User user) {
        OneTimeToken token = new OneTimeToken(user);
        OneTimeToken oneTimeToken = oneTimeTokenRepository.save(token);

        return oneTimeToken.getUuid();
    }
}
