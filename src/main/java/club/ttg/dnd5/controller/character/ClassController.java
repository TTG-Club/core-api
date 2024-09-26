package club.ttg.dnd5.controller.character;

//import club.ttg.dnd5.dto.character.ClassRequest;
//import club.ttg.dnd5.dto.character.ClassResponse;
//import club.ttg.dnd5.dto.engine.SearchRequest;
//import club.ttg.dnd5.service.character.ClassService;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import lombok.RequiredArgsConstructor;
//import org.springdoc.core.annotations.ParameterObject;
//import org.springframework.http.HttpStatus;
//import org.springframework.security.access.annotation.Secured;
//import org.springframework.web.bind.annotation.*;

//@Tag(name = "Классы", description = "REST API классов персонажа")
//
//@RestController
//@RequiredArgsConstructor
//@RequestMapping("/api/v2/class")
//public class ClassController {
//    private final ClassService classService;
//
//    @Operation(summary = "Получение краткого списка классов")
//    @GetMapping
//    public Collection<ClassResponse> getClasses(@ParameterObject final SearchRequest request) {
//        return classService.getClasses(request);
//    }
//
//    @Operation(summary = "Получение краткого списка подклассов")
//    @GetMapping("/{url}/subclasses")
//    public Collection<ClassResponse> getSubclasses(@PathVariable String classUrl) {
//        return classService.getSubClasses(classUrl);
//    }
//
//    @Operation(summary = "Получение детального описания класса")
//    @GetMapping("/{url}")
//    public ClassResponse getClass(@PathVariable String url) {
//        return classService.getClass(url);
//    }
//
//    @Operation(summary = "Добавление класса или подкласса")
//    @ResponseStatus(HttpStatus.CREATED)
//    @Secured("ROLE_ADMIN")
//    @PostMapping
//    public ClassResponse addClass(@RequestBody final ClassRequest request) {
//        return classService.addClass(request);
//    }
//
//    @ResponseStatus(HttpStatus.OK)
//    @Secured("ROLE_ADMIN")
//    @PutMapping("/{url}")
//    public ClassResponse updateClass(
//            @PathVariable final String url,
//            @RequestBody final ClassRequest request) {
//        return classService.updateClass(url, request);
//    }
//}
