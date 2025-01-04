package club.ttg.dnd5.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ResponseDto {
    Integer status;
    String error;
    String message;
}
