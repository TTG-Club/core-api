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

    @Test
    void wideImageIsScaledDownProportionally() throws IOException {
        BufferedImage result = decode(ImageConverter.toWebp(write(sized(4096, 1024, false), "png"),
                "image/png", "wide.png", WebpOptions.lossless().resizeToFit(2048, 2048)).bytes());

        assertEquals(2048, result.getWidth());
        assertEquals(512, result.getHeight());
    }

    @Test
    void tallImageIsScaledDownProportionally() throws IOException {
        BufferedImage result = decode(ImageConverter.toWebp(write(sized(1000, 3000, false), "png"),
                "image/png", "tall.png", WebpOptions.lossless().resizeToFit(2048, 2048)).bytes());

        assertEquals(683, result.getWidth());
        assertEquals(2048, result.getHeight());
    }

    @Test
    void smallImageKeepsItsSize() throws IOException {
        BufferedImage result = decode(ImageConverter.toWebp(write(sized(800, 600, false), "png"),
                "image/png", "small.png", WebpOptions.lossless().resizeToFit(2048, 2048)).bytes());

        assertEquals(800, result.getWidth());
        assertEquals(600, result.getHeight());
    }

    @Test
    void transparencySurvivesResize() throws IOException {
        BufferedImage result = decode(ImageConverter.toWebp(write(sized(5000, 2500, true), "png"),
                "image/png", "alpha.png", WebpOptions.lossless().resizeToFit(2048, 2048)).bytes());

        assertEquals(2048, result.getWidth());
        assertEquals(1024, result.getHeight());
        assertEquals(0, result.getRGB(result.getWidth() - 1, 0) >>> 24, "правая половина должна остаться прозрачной");
    }

    @Test
    void extremelyThinImageDoesNotCollapseToZero() throws IOException {
        BufferedImage result = decode(ImageConverter.toWebp(write(sized(9000, 1, false), "png"),
                "image/png", "line.png", WebpOptions.lossless().resizeToFit(2048, 2048)).bytes());

        assertEquals(2048, result.getWidth());
        assertEquals(1, result.getHeight());
    }

    /** Картинка заданного размера; с alpha — левая половина залита, правая прозрачная. */
    static BufferedImage sized(int w, int h, boolean alpha) {
        BufferedImage image = new BufferedImage(w, h, alpha ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(Color.ORANGE);
        g.fillRect(0, 0, alpha ? w / 2 : w, h);
        g.dispose();
        return image;
    }

    private static BufferedImage decode(byte[] bytes) throws IOException {
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes));
        assertNotNull(image);
        return image;
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

    static byte[] write(BufferedImage image, String format) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, format, out);
        return out.toByteArray();
    }
}
