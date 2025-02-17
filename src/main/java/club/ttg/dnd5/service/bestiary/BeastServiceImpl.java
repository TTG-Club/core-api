package club.ttg.dnd5.service.bestiary;

import club.ttg.dnd5.repository.bestiary.BeastRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BeastServiceImpl implements BeastService{

    private final BeastRepository beastRepository;

    @Override
    public boolean beastExistsByUrl(final String url) {
        return beastRepository.existsById(url);
    }
}
