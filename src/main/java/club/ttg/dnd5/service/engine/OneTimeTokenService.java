package club.ttg.dnd5.service.engine;

import club.ttg.dnd5.model.user.OneTimeToken;
import club.ttg.dnd5.model.user.User;
import club.ttg.dnd5.repository.user.OneTimeTokenRepository;
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
