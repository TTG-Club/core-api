package club.ttg.dnd5.controller.species;

import club.ttg.dnd5.dto.engine.SearchRequest;
import club.ttg.dnd5.dto.species.SpeciesResponse;
import club.ttg.dnd5.service.species.SpeciesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/species")
public class SpeciesController {
    private final SpeciesService speciesService;

    @GetMapping
    public ResponseEntity<List<SpeciesResponse>> getAllSpecies() {
        List<SpeciesResponse> speciesList = speciesService.findAll();
        return new ResponseEntity<>(speciesList, HttpStatus.OK);
    }

    @GetMapping("/{url}")
    public ResponseEntity<SpeciesResponse> getSpeciesByUrl(@PathVariable String url) {
        return speciesService.findById(url)
                .map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    @Secured("ROLE_ADMIN")
    public ResponseEntity<SpeciesResponse> createSpecies(@RequestBody SpeciesResponse speciesResponse) {
        SpeciesResponse createdSpecies = speciesService.save(speciesResponse);
        return new ResponseEntity<>(createdSpecies, HttpStatus.CREATED);
    }

    @PutMapping("/{url}")
    @Secured("ROLE_ADMIN")
    public ResponseEntity<SpeciesResponse> updateSpecies(@PathVariable String url, @RequestBody SpeciesResponse speciesResponse) {
        speciesResponse.setUrl(url);  // Ensure the URL in the path and the body match
        SpeciesResponse updatedSpecies = speciesService.update(speciesResponse);
        return new ResponseEntity<>(updatedSpecies, HttpStatus.OK);

    }

    @DeleteMapping("/{url}")
    @Secured("ROLE_ADMIN")
    public ResponseEntity<Void> deleteSpecies(@PathVariable String url) {
        speciesService.deleteById(url);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/search")
    public ResponseEntity<List<SpeciesResponse>> searchSpecies(@RequestBody SearchRequest request) {
        List<SpeciesResponse> speciesList = speciesService.searchSpecies(request);
        return new ResponseEntity<>(speciesList, HttpStatus.OK);
    }
}