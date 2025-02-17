package club.ttg.dnd5.controller.bestiary;

import club.ttg.dnd5.service.bestiary.BeastService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Бестиарий", description = "REST API для существ из бестиария")

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/bestiary")
public class BeastController {

    private final BeastService beastService;

    /**
     * Проверка существования существа по URL.
     *
     * @param url URL существа.
     * @return 204, если существа с таким URL не существует; 409, если  существует.
     */
    @Operation(
            summary = "Проверка существования существа",
            description = "Возвращает 204 (No Content), если существо с указанным URL не существует, или 409 (Conflict), если существует."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Существо с указанным URL не найден."),
            @ApiResponse(responseCode = "409", description = "Существо с указанным URL уже существует.")
    })
    @RequestMapping(value = "/{url}", method = RequestMethod.HEAD)
    public ResponseEntity<Void> handleOptions(@PathVariable("url") String url) {
        boolean exists = beastService.beastExistsByUrl(url);
        if (exists) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } else {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
    }
}
