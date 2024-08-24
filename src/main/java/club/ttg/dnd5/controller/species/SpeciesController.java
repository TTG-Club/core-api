package club.ttg.dnd5.controller.species;

import club.ttg.dnd5.dto.engine.SearchRequest;
import club.ttg.dnd5.dto.species.SpeciesDTO;
import club.ttg.dnd5.service.species.SpeciesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/species")
public class SpeciesController {
    private final SpeciesService speciesService;

    @GetMapping
    public ResponseEntity<List<SpeciesDTO>> getAllSpecies() {
        List<SpeciesDTO> speciesList = speciesService.findAll();
        return new ResponseEntity<>(speciesList, HttpStatus.OK);
    }

    @GetMapping("/{url}")
    public ResponseEntity<SpeciesDTO> getSpeciesByUrl(@PathVariable String url) {
        return speciesService.findById(url)
                .map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public ResponseEntity<SpeciesDTO> createSpecies(@RequestBody SpeciesDTO speciesDTO) {
        SpeciesDTO createdSpecies = speciesService.save(speciesDTO);
        return new ResponseEntity<>(createdSpecies, HttpStatus.CREATED);
    }

    @PutMapping("/{url}")
    public ResponseEntity<SpeciesDTO> updateSpecies(@PathVariable String url, @RequestBody SpeciesDTO speciesDTO) {
        speciesDTO.setUrl(url);  // Ensure the URL in the path and the body match
        try {
            SpeciesDTO updatedSpecies = speciesService.update(speciesDTO);
            return new ResponseEntity<>(updatedSpecies, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{url}")
    public ResponseEntity<Void> deleteSpecies(@PathVariable String url) {
        speciesService.deleteById(url);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/search")
    public ResponseEntity<List<SpeciesDTO>> searchSpecies(@RequestBody SearchRequest request) {
        List<SpeciesDTO> speciesList = speciesService.searchSpecies(request);
        return new ResponseEntity<>(speciesList, HttpStatus.OK);
    }
}