package club.ttg.dnd5.domain.image.service;

import club.ttg.dnd5.domain.image.service.ImageConverter.EncodedImage;
import club.ttg.dnd5.domain.image.service.ImageConverter.SourceFormat;
import club.ttg.dnd5.domain.image.service.ImageConverter.WebpOptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Перекодирование обложки под площадки без поддержки WebP (загрузка фото на стену ВКонтакте).
 * Обложки на сайте всегда лежат в S3 в WebP, а VK принимает только JPG / PNG / GIF.
 */
class ImageConverterTest {

    @BeforeAll
    static void registerPlugins() {
        // В приложении это делает ImageService#init; в тесте нужен тот же реестр (WebP-плагин).
        ImageIO.scanForPlugins();
    }

    @Test
    void opaqueWebpBecomesJpeg() throws IOException {
        byte[] webp = webp(opaqueImage());

        EncodedImage encoded = ImageConverter.toJpegOrPng(webp, 0.9f);

        assertEquals("image/jpeg", encoded.contentType());
        assertEquals("jpg", encoded.extension());
        assertEquals(SourceFormat.JPEG, SourceFormat.fromSignature(encoded.bytes()).orElse(SourceFormat.UNKNOWN));
        assertNotNull(ImageIO.read(new ByteArrayInputStream(encoded.bytes())));
    }

    @Test
    void transparentWebpBecomesPng() throws IOException {
        byte[] webp = webp(transparentImage());

        EncodedImage encoded = ImageConverter.toJpegOrPng(webp, 0.9f);

        assertEquals("image/png", encoded.contentType());
        assertEquals("png", encoded.extension());
        assertEquals(SourceFormat.PNG, SourceFormat.fromSignature(encoded.bytes()).orElse(SourceFormat.UNKNOWN));
    }

    @Test
    void jpegAndPngPassThroughUntouched() throws IOException {
        byte[] png = write(transparentImage(), "png");
        byte[] jpeg = write(opaqueImage(), "jpg");

        EncodedImage fromPng = ImageConverter.toJpegOrPng(png, 0.9f);
        EncodedImage fromJpeg = ImageConverter.toJpegOrPng(jpeg, 0.9f);

        assertArrayEquals(png, fromPng.bytes());
        assertEquals("png", fromPng.extension());
        assertArrayEquals(jpeg, fromJpeg.bytes());
        assertEquals("jpg", fromJpeg.extension());
    }

    private static BufferedImage opaqueImage() {
        BufferedImage image = new BufferedImage(16, 9, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(Color.RED);
        g.fillRect(0, 0, 16, 9);
        g.dispose();
        return image;
    }

    private static BufferedImage transparentImage() {
        BufferedImage image = new BufferedImage(16, 9, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setColor(new Color(0, 0, 255, 128));
        g.fillRect(0, 0, 8, 9);
        g.dispose();
        return image;
    }

    private static byte[] webp(BufferedImage image) throws IOException {
        return ImageConverter.toWebp(write(image, "png"), "image/png", "cover.png", WebpOptions.lossless()).bytes();
    }

    private static byte[] write(BufferedImage image, String format) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, format, out);
        return out.toByteArray();
    }
}
