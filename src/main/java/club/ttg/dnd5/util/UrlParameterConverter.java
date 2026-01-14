package club.ttg.dnd5.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.zip.Inflater;

@Slf4j
@UtilityClass
public class UrlParameterConverter {

    public String decompression(String compressedString) {
        if (compressedString == null || compressedString.isEmpty()) {
            return null;
        }

        try {
            byte[] compressed = Base64.getDecoder().decode(compressedString);

            Inflater inflater = new Inflater();
            inflater.setInput(compressed);

            byte[] buffer = new byte[1024];
            StringBuilder result = new StringBuilder();

            while (!inflater.finished()) {
                int count = inflater.inflate(buffer);
                result.append(new String(buffer, 0, count, StandardCharsets.UTF_8));
            }

            inflater.end();
            return result.toString();

        } catch (Exception e) {
            throw new RuntimeException("Error decompressing filters", e);
        }
    }
}

