package club.ttg.dnd5.domain.image.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.s3.S3Client;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

/**
 * Картинки больше 2048 px по любой стороне не отклоняются, а пропорционально уменьшаются
 * перед сохранением в S3 ({@code convert} идёт тем же путём, что и {@code upload}, только без S3).
 */
class ImageServiceResizeTest {

    private ImageService service;

    @BeforeEach
    void setUp() {
        ImageIO.scanForPlugins();
        service = new ImageService(mock(S3Client.class));
        ReflectionTestUtils.setField(service, "maxBytes", 10L * 1024 * 1024);
        ReflectionTestUtils.setField(service, "maxWidth", 2048);
        ReflectionTestUtils.setField(service, "maxHeight", 2048);
        ReflectionTestUtils.setField(service, "maxSourcePixels", 50_000_000L);
        ReflectionTestUtils.setField(service, "allowedTypes", List.of("png", "jpg", "jpeg", "webp", "bmp"));
        ReflectionTestUtils.setField(service, "webpQuality", 0.82f);
        ReflectionTestUtils.setField(service, "webpLossless", false);
        ReflectionTestUtils.setField(service, "webpPreserveAlpha", true);
    }

    @Test
    void oversizedImageIsAcceptedAndScaledTo2048() throws IOException {
        BufferedImage out = convert(3000, 4000);

        assertEquals(1536, out.getWidth());
        assertEquals(2048, out.getHeight());
    }

    @Test
    void imageWithinLimitIsNotResized() throws IOException {
        BufferedImage out = convert(2048, 1000);

        assertEquals(2048, out.getWidth());
        assertEquals(1000, out.getHeight());
    }

    @Test
    void absurdlyLargeSourceIsStillRejected() throws IOException {
        ReflectionTestUtils.setField(service, "maxSourcePixels", 1_000_000L);
        MockMultipartFile file = png(1500, 1000);

        assertThrows(ImageService.ImageValidationException.class, () -> service.convert(file));
    }

    private BufferedImage convert(int w, int h) throws IOException {
        byte[] webp = service.convert(png(w, h));
        return ImageIO.read(new ByteArrayInputStream(webp));
    }

    private static MockMultipartFile png(int w, int h) throws IOException {
        byte[] bytes = ImageConverterTest.write(ImageConverterTest.sized(w, h, false), "png");
        return new MockMultipartFile("file", "big.png", "image/png", bytes);
    }
}
