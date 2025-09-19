package club.ttg.dnd5.domain.charlist.rest.controller;

import club.ttg.dnd5.domain.charlist.rest.dto.CharListDetailedResponse;
import club.ttg.dnd5.domain.charlist.rest.dto.CharListRequest;
import club.ttg.dnd5.domain.charlist.rest.dto.CharListShortResponse;
import club.ttg.dnd5.domain.charlist.service.CharListService;
import club.ttg.dnd5.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Collection;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/charlist")
public class CharListController {
    private static final int MAX_CHAR_LIST = 3;

    private final CharListService charListService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public Collection<CharListShortResponse> getAllByUser(Principal principal) {
        return charListService.getAllByUser(principal.getName());
    }

    @GetMapping("/{id}")
    public CharListDetailedResponse getCharList(@PathVariable String id) {
        return charListService.getById(id);
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping
    public String save(@RequestBody CharListRequest request,
                       @AuthenticationPrincipal UserDetails userDetails) {
        boolean subscriber = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(s -> s.equals("SUBSCRIBER"));
        long count = charListService.countCurrentCharList(userDetails.getUsername());
        if (!subscriber && count >= MAX_CHAR_LIST) {
            throw new ApiException(HttpStatus.PAYMENT_REQUIRED, "Достигнуто максимальное количество листов персонажа");
        }
        return charListService.save(request);
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping
    public String save(@RequestBody CharListRequest request, Principal principal) {
        return charListService.update(request);
    }
}
