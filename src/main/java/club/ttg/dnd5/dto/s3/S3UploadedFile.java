package club.ttg.dnd5.dto.s3;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class S3UploadedFile {
    private String filename;
    private String url;
}
