package club.ttg.dnd5.util;

import lombok.experimental.UtilityClass;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

@UtilityClass
public class UrlParameterConverter
{
    public String decompress(String compressedString)
    {
        if (compressedString == null || compressedString.isBlank())
        {
            return null;
        }


        byte[] compressed;
        try  {
            compressed = Base64.getUrlDecoder().decode(compressedString);
        }
        catch (IllegalArgumentException exception) {
            throw new RuntimeException("Invalid Base64 value: [" + compressedString + "]", exception);
        }

        Inflater inflater = new Inflater();

        try {
            inflater.setInput(compressed);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];

            while (!inflater.finished()) {
                int count = inflater.inflate(buffer);

                if (count == 0) {
                    if (inflater.needsInput()) {
                        throw new RuntimeException("Inflater needs more input");
                    }

                    if (inflater.needsDictionary()) {
                        throw new RuntimeException("Dictionary is required for decompression");
                    }

                    throw new RuntimeException("Unable to decompress input data");
                }

                outputStream.write(buffer, 0, count);
            }

            return outputStream.toString(StandardCharsets.UTF_8);
        }
        catch (DataFormatException exception) {
            throw new RuntimeException("Invalid compressed data format", exception);
        }
        finally {
            inflater.end();
        }
    }
}