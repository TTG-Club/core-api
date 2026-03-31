package club.ttg.dnd5.domain.user.rest.controller;

import club.ttg.dnd5.domain.user.model.Role;
import club.ttg.dnd5.domain.user.repository.RoleRepository;
import club.ttg.dnd5.domain.user.rest.dto.RoleRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Управление ролями")
@Secured("ADMIN")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v2/roles")
public class RoleController {
    private final RoleRepository roleRepository;

    @GetMapping
    public List<Role> getRoles() {
        return roleRepository.findAll();
    }

    @PostMapping
    public void saveRole(@RequestBody RoleRequest request) {
        roleRepository.save(Role.builder().name(request.getName()).build());
    }

    @PutMapping
    public void updateRole(long id, @RequestBody RoleRequest request) {
        roleRepository.save(Role.builder().id(id).name(request.getName()).build());
    }
}
