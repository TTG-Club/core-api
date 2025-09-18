package club.ttg.dnd5.domain.common.rest.controller;

import club.ttg.dnd5.domain.common.model.notification.Persona;
import club.ttg.dnd5.domain.common.repository.PersonaRepository;
import club.ttg.dnd5.domain.common.rest.dto.notification.PersonaRequest;
import club.ttg.dnd5.domain.common.rest.dto.notification.PersonaResponse;
import club.ttg.dnd5.domain.common.rest.mapper.PersonaMapper;
import club.ttg.dnd5.exception.EntityNotFoundException;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Персона для нотификаций", description = "API для персоны")
@RestController
@RequestMapping("/api/v2/persona")
@RequiredArgsConstructor
public class PersonaController {
    private final PersonaRepository personaRepository;
    private final PersonaMapper personaMapper;

    @GetMapping
    public List<PersonaResponse> getAll() {
        return personaRepository.findAll().stream()
                .map(personaMapper::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public PersonaResponse getById(@PathVariable UUID id) {
        return personaMapper.toResponse(personaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Персона не найдена"))
        );
    }

    @PostMapping
    public String create(@RequestBody Persona persona) {
        return personaRepository.save(persona).getId();
    }

    @PutMapping("/{id}")
    public String update(@PathVariable UUID id, @RequestBody PersonaRequest request) {
        return personaRepository.findById(id)
                .map(existing -> {
                    personaMapper.updateEntity(request, existing);
                    return personaRepository.save(existing);
                })
                .orElseThrow(() -> new EntityNotFoundException("Персона не найдена"))
                .getId();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        if (personaRepository.existsById(id)) {
            personaRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
