package club.ttg.dnd5.service.character;

import club.ttg.dnd5.dto.character.ClassRequest;
import club.ttg.dnd5.dto.character.ClassResponse;
import club.ttg.dnd5.exception.PageNotFoundException;
import club.ttg.dnd5.repository.ClassRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class ClassServiceImpl implements ClassService {
    private final ClassRepository classRepository;
    private final ModelMapper modelMapper;

    @Override
    public ClassResponse getClass(String url) {
        return modelMapper.map(
                classRepository.findById(url).orElseThrow(PageNotFoundException::new),
                ClassResponse.class);
    }

    @Transactional
    @Override
    public ClassResponse addClass(ClassRequest request) {
        return null;
    }

    @Transactional
    @Override
    public ClassResponse updateClass(String url, ClassRequest request) {
        var classChar = classRepository.findById(url).orElseThrow(PageNotFoundException::new);

        return null;
    }
}
