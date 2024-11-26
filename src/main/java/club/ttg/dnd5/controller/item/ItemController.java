package club.ttg.dnd5.controller.item;

import club.ttg.dnd5.dto.item.ItemDto;
import club.ttg.dnd5.service.item.ItemService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/item")
@Tag(name = "Снаряжение", description = "REST API снаряжение и прочие предметы")
public class ItemController {
    private final ItemService itemService;

    @GetMapping("/{url}")
    public ItemDto getItem() {
        return null;
    }
}
