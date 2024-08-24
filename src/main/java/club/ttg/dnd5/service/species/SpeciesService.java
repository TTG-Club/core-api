package club.ttg.dnd5.service.species;

import club.ttg.dnd5.dto.species.SpeciesDTO;
import club.ttg.dnd5.exception.StorageException;
import club.ttg.dnd5.mapper.species.SpeciesMapper;
import club.ttg.dnd5.model.character.Species;
import club.ttg.dnd5.repository.SpeciesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class SpeciesService {

    private final SpeciesRepository speciesRepository;
    private final SpeciesMapper speciesMapper = SpeciesMapper.INSTANCE;

    @Autowired
    public SpeciesService(SpeciesRepository speciesRepository) {
        this.speciesRepository = speciesRepository;
    }

    public List<SpeciesDTO> findAll() {
        return speciesRepository.findAll().stream()
                .map(speciesMapper::speciesToSpeciesDTO)
                .collect(Collectors.toList());
    }

    public Optional<SpeciesDTO> findById(String url) {
        return speciesRepository.findById(url)
                .map(speciesMapper::speciesToSpeciesDTO);
    }

    public SpeciesDTO save(SpeciesDTO speciesDTO) {
        Species species = speciesMapper.speciesDTOToSpecies(speciesDTO);
        Species savedSpecies = speciesRepository.save(species);
        return speciesMapper.speciesToSpeciesDTO(savedSpecies);
    }

    public SpeciesDTO update(SpeciesDTO speciesDTO) {
        if (speciesRepository.existsById(speciesDTO.getUrl())) {
            Species species = speciesMapper.speciesDTOToSpecies(speciesDTO);
            Species updatedSpecies = speciesRepository.save(species);
            return speciesMapper.speciesToSpeciesDTO(updatedSpecies);
        } else {
            throw new StorageException("Species with url " + speciesDTO.getUrl() + " does not exist.");
        }
    }

    public void deleteById(String url) {
        speciesRepository.deleteById(url);
    }
}
