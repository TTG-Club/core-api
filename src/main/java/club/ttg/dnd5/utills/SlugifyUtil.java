package club.ttg.dnd5.utills;

import com.github.slugify.Slugify;
import org.apache.commons.io.FilenameUtils;

public class SlugifyUtil {
    private final static Slugify slugify = Slugify.builder()
            .transliterator(true)
            .build();

    public static String getSlug(String text) {
        return slugify.slugify(text);
    }

    public static String getFilenameSlug(String originalFilename) {
        String filename = FilenameUtils.getBaseName(originalFilename);
        String extension = FilenameUtils.getExtension(originalFilename);

        if (extension == null) {
            return getSlug(filename);
        }

        return String.format("%s.%s", getSlug(filename), extension);
    }
}
