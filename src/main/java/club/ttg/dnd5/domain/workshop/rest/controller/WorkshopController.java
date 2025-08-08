package club.ttg.dnd5.domain.workshop.rest.controller;

import club.ttg.dnd5.domain.user.rest.dto.UserDto;
import club.ttg.dnd5.domain.workshop.rest.dto.WorkshopDto;
import club.ttg.dnd5.domain.workshop.service.WorkshopService;
import club.ttg.dnd5.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Мастерская", description = "REST API мастерской")

@RestController
@RequestMapping("/api/workshop")
@RequiredArgsConstructor
public class WorkshopController {

    private final WorkshopService workshopService;

    @Operation(summary = "Получение количества страниц созданных пользователем по разделу")
    @GetMapping("/statistic")
    public List<WorkshopDto> getWorkshopStatistics() {
        UserDto userDto = SecurityUtils.getUserDto();

        return workshopService.getWorkshopUserStatistics(userDto.getUsername());
    }
}
