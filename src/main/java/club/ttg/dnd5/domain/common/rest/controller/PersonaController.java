package club.ttg.dnd5.domain.common.rest.controller;

import club.ttg.dnd5.domain.common.model.notification.Persona;
import club.ttg.dnd5.domain.common.repository.PersonaRepository;
import club.ttg.dnd5.domain.common.rest.dto.notification.PersonaRequest;
import club.ttg.dnd5.domain.common.rest.dto.notification.PersonaResponse;
import club.ttg.dnd5.domain.common.rest.mapper.PersonaMapper;
import club.ttg.dnd5.exception.EntityNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Tag(name = "Персона для нотификаций", description = "API для персоны")
@RestController
@RequestMapping("/api/v2/persona")
@RequiredArgsConstructor
public class PersonaController {
    private final PersonaRepository personaRepository;
    private final PersonaMapper personaMapper;

    @Operation(summary = "Получение списка всех персон")
    @GetMapping
    public List<PersonaResponse> getAll() {
        return personaRepository.findAllByOrderByCreatedAtAsc().stream()
                .map(personaMapper::toResponse)
                .toList();
    }

    @Operation(summary = "Получение персоны по ID")
    @GetMapping("/{id}")
    public PersonaResponse getById(@PathVariable UUID id) {
        return personaMapper.toResponse(personaRepository.findById(Objects.requireNonNull(id))
                .orElseThrow(() -> new EntityNotFoundException("Персона не найдена"))
        );
    }

    @Operation(summary = "Создание новой персоны")
    @PostMapping
    public String create(@RequestBody PersonaRequest request) {
        Persona persona = Objects.requireNonNull(personaMapper.toEntity(request));
        return personaRepository.save(persona).getId().toString();
    }

    @Operation(summary = "Частичное обновление данных персоны по ID")
    @PatchMapping("/{id}")
    public String update(@PathVariable UUID id, @RequestBody PersonaRequest request) {
        return personaRepository.findById(Objects.requireNonNull(id))
                .map(existing -> {
                    personaMapper.updateEntity(request, existing);
                    return personaRepository.save(existing);
                })
                .orElseThrow(() -> new EntityNotFoundException("Персона не найдена"))
                .getId().toString();
    }

    @Operation(summary = "Удаление персоны по ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        if (personaRepository.existsById(Objects.requireNonNull(id))) {
            personaRepository.deleteById(Objects.requireNonNull(id));
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
