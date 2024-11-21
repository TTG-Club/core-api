package club.ttg.dnd5.controller.character;

import club.ttg.dnd5.dto.character.FeatDto;
import club.ttg.dnd5.service.character.FeatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@Tag(name = "Черты ", description = "REST API черты персонажа")

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/trait")
public class FeatController {
    private final FeatService featService;

    @Operation(summary = "Получение детального описания черты")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Черта успешно получена"),
            @ApiResponse(responseCode = "404", description = "Черта не найдена")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("{/featUrl}")
    public FeatDto getFeat(@PathVariable final String featUrl) {
        return featService.getFeat(featUrl);
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/search")
    public Collection<FeatDto> getFeats() {
        return featService.getFeats();
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public FeatDto addFeats(@RequestBody final FeatDto featDto) {
        return featService.addFeat(featDto);
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping
    public FeatDto updateFeats(@RequestBody final FeatDto featDto) {
        return featService.updateFeat(featDto);
    }

    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("{featUrl}")
    public FeatDto deleteFeats(@PathVariable final String featUrl) {
        return featService.delete(featUrl);
    }
}
