package club.ttg.dnd5.util;

import lombok.experimental.UtilityClass;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

@UtilityClass
public class UrlParameterConverter {

    public String decompression(String compressed) {
        if (compressed == null || compressed.isEmpty()) {
            return null;
        }

        StringBuilder base64 = new StringBuilder(
                compressed
                        .replace('-', '+')
                        .replace('_', '/')
        );

        while (base64.length() % 4 != 0) {
            base64.append('=');
        }

        byte[] compressedBytes = Base64.getDecoder().decode(base64.toString());

        Inflater inflater = new Inflater();
        inflater.setInput(compressedBytes);

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];

            while (!inflater.finished()) {
                int count = inflater.inflate(buffer);

                if (count == 0) {
                    break;
                }

                outputStream.write(buffer, 0, count);
            }

            return outputStream.toString(StandardCharsets.UTF_8);

        } catch (DataFormatException | IOException e) {
            throw new RuntimeException("Failed to decompress URL parameter", e);
        } finally {
            inflater.end();
        }
    }
}

