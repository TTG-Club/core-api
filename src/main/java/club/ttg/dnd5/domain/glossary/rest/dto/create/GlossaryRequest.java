package club.ttg.dnd5.domain.glossary.rest.dto.create;

import club.ttg.dnd5.domain.common.rest.dto.BaseRequest;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GlossaryRequest extends BaseRequest {
    private String tagCategory;
}