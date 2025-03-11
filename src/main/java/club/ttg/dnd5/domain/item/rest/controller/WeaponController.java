package club.ttg.dnd5.domain.item.rest.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/weapon")
@Tag(name = "Оружие", description = "REST API для свойств и приемов оружия")
public class WeaponController {

    @PostMapping("/property")
    public String addProperty() {
        return null;
    }

    @PostMapping("/mastery")
    public String addMastery() {
        return null;
    }
}
