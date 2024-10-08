package club.ttg.dnd5.service.character;

import club.ttg.dnd5.dto.character.ClassRequest;
import club.ttg.dnd5.dto.character.ClassResponse;
import club.ttg.dnd5.dto.engine.SearchRequest;
import club.ttg.dnd5.exception.PageExistException;
import club.ttg.dnd5.exception.PageNotFoundException;
import club.ttg.dnd5.mapper.character.ClassMapper;
import club.ttg.dnd5.repository.ClassRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;

@RequiredArgsConstructor
@Service
public class ClassServiceImpl implements ClassService {
    private final ClassRepository classRepository;

    @Override
    public Collection<ClassResponse> getClasses(final SearchRequest request) {

        return null;
    }

    @Override
    public Collection<ClassResponse> getSubClasses(final String url) {
        return classRepository.findAllSubclasses(url);
    }

    @Override
    public ClassResponse getClass(final String url) {
        return ClassMapper.MAPPER.toResponse(
            classRepository.findById(url).orElseThrow(PageNotFoundException::new)
        );
    }

    @Transactional
    @Override
    public ClassResponse addClass(final ClassRequest request) {
        classRepository.findById(request.getUrl()).ifPresent(c -> { throw new PageExistException();});
        var classChar = ClassMapper.MAPPER.toEntity(request);
        classChar.setCreated(LocalDateTime.now());
        classChar.setLastUpdated(LocalDateTime.now());
        var saved = classRepository.save(classChar);
        return ClassMapper.MAPPER.toResponse(saved);
    }

    @Transactional
    @Override
    public ClassResponse updateClass(final String url, final ClassRequest request) {
        var classChar = classRepository.findById(url).orElseThrow(PageNotFoundException::new);
        return null;
    }
}
