package club.ttg.dnd5.domain.item.rest.controller;

import club.ttg.dnd5.domain.item.model.weapon.WeaponMastery;
import club.ttg.dnd5.domain.item.model.weapon.WeaponProperties;
import club.ttg.dnd5.domain.item.repository.WeaponMasteryRepository;
import club.ttg.dnd5.domain.item.repository.WeaponPropertiesRepository;
import club.ttg.dnd5.exception.EntityExistException;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@Tag(name = "Оружие", description = "REST API для свойств и приемов оружия")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/weapon")
public class WeaponController {
    private final WeaponPropertiesRepository weaponPropertiesRepository;
    private final WeaponMasteryRepository weaponMasteryRepository;

    @GetMapping("property")
    public Collection<WeaponProperties> getAllWeaponProperties() {
        return weaponPropertiesRepository.findAll();
    }

    @GetMapping("mastery")
    public Collection<WeaponMastery> getAllWeaponMastery() {
        return weaponMasteryRepository.findAll();
    }

    @Secured("ADMIN")
    @PostMapping("/property")
    public String addProperty(@RequestBody WeaponProperties weaponProperties) {
        var property = weaponPropertiesRepository.findById(weaponProperties.getUrl());
        if (property.isPresent()) {
            throw new EntityExistException("Свойство оружия с таким URL уже существует");
        }
        return weaponPropertiesRepository.save(weaponProperties).getUrl();
    }

    @Secured("ADMIN")
    @PostMapping("/mastery")
    public String addMastery(@RequestBody WeaponMastery weaponMastery) {
        var mastery = weaponMasteryRepository.findById(weaponMastery.getUrl());
        if (mastery.isPresent()) {
            throw new EntityExistException("Свойство оружия с таким URL уже существует");
        }
        return weaponMasteryRepository.save(weaponMastery).getUrl();
    }
}
