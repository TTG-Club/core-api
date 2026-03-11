package club.ttg.dnd5.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

@Slf4j
@UtilityClass
public class UrlParameterConverter
{
    public String decompress(String compressedString)
    {
        if (compressedString == null || compressedString.isBlank())
        {
            return null;
        }

        Inflater inflater = new Inflater();

        try
        {
            String decodedUrlValue = URLDecoder.decode(compressedString, StandardCharsets.UTF_8);
            byte[] compressed = Base64.getDecoder().decode(decodedUrlValue);

            inflater.setInput(compressed);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];

            while (!inflater.finished())
            {
                int count = inflater.inflate(buffer);

                if (count == 0)
                {
                    if (inflater.needsInput())
                    {
                        break;
                    }

                    if (inflater.needsDictionary())
                    {
                        throw new RuntimeException("Dictionary is required for decompression");
                    }

                    throw new RuntimeException("Unable to decompress input data");
                }

                outputStream.write(buffer, 0, count);
            }

            return outputStream.toString(StandardCharsets.UTF_8);
        }
        catch (IllegalArgumentException e)
        {
            throw new RuntimeException("Invalid Base64 value in URL parameter", e);
        }
        catch (DataFormatException e)
        {
            throw new RuntimeException("Invalid compressed data format", e);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Error decompressing filters", e);
        }
        finally
        {
            inflater.end();
        }
    }
}