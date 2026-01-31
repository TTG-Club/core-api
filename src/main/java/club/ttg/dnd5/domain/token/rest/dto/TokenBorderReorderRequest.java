package club.ttg.dnd5.domain.token.rest.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class TokenBorderReorderRequest
{
    @NotNull
    private UUID id;

    @Min(1)
    private int order;
}
