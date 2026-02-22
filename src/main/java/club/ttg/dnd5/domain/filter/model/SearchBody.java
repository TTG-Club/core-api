package club.ttg.dnd5.domain.filter.model;

import club.ttg.dnd5.util.UrlParameterConverter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.StringUtils;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SearchBody {

    private FilterInfo sources;
    private FilterInfo filter;


    public static SearchBody parse(final String filter, ObjectMapper objectMapper) {
        if (!StringUtils.hasText(filter)) {
            return new SearchBody(new FilterInfo(), new FilterInfo());
        }

        final String json;
        try {
            json = UrlParameterConverter.decompression(filter);
        } catch (Exception e) {
            return new SearchBody(new FilterInfo(), new FilterInfo());
        }

        if (!StringUtils.hasText(json)) {
            return new SearchBody(new FilterInfo(), new FilterInfo());
        }

        try {
            return objectMapper.readValue(json, SearchBody.class);
        } catch (JsonProcessingException e) {
            return new SearchBody(new FilterInfo(), new FilterInfo());
        }
    }
}
