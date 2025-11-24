package club.ttg.dnd5.domain.roadmap.controller;

import club.ttg.dnd5.domain.roadmap.model.Roadmap;
import club.ttg.dnd5.domain.roadmap.rest.dto.RoadmapResponse;
import club.ttg.dnd5.domain.roadmap.service.RoadmapService;
import club.ttg.dnd5.security.SecurityUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.Set;

@Tag(name = "Дорожная карта")
@RestController
@RequestMapping("/api/v2/roadmap")
@RequiredArgsConstructor
public class RoadmapController {
    private static final Set<String> ROLES = Set.of("ADMIN", "MODERATOR");

    private final RoadmapService roadmapService;

    @GetMapping
    public Collection<RoadmapResponse> findAll() {
        var allVisible = SecurityUtils.getUser().getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(ROLES::contains);
        return roadmapService.findAll(allVisible);
    }

    @GetMapping("/{url}")
    public RoadmapResponse finOne(@PathVariable String url) {
        return roadmapService.findOne(url);
    }

    @Secured("ADMIN")
    @PostMapping
    public String save(@RequestBody Roadmap roadmap) {
        return roadmapService.save(roadmap);
    }

    @Secured("ADMIN")
    @PutMapping
    public String update(@RequestBody Roadmap roadmap) {
        return roadmapService.update(roadmap);
    }

    @Secured("ADMIN")
    @DeleteMapping("/{url}")
    public String remove(String url) {
        return roadmapService.remove(url);
    }
}
