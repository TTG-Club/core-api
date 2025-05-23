package club.ttg.dnd5.domain.cache.rest.controller;


import club.ttg.dnd5.domain.cache.service.CacheService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@Tag(name = "Кэш", description = "API для управление кэшем")
@RequestMapping("/api/v2/cache/")
@RestController
public class CacheController {
    private final CacheService cacheService;

    @Operation(summary = "Очистка кэша")
    @GetMapping("/evict-all")
    @Secured("ADMIN")
    public void evictAll() {
        cacheService.evictCaches();
    }
}
