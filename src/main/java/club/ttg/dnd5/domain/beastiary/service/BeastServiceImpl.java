package club.ttg.dnd5.domain.beastiary.service;

import club.ttg.dnd5.domain.beastiary.repository.BeastRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class BeastServiceImpl implements BeastService {
    private final BeastRepository beastRepository;

    @Override
    public boolean exist(final String url) {
        return beastRepository.existsById(url);
    }
}
