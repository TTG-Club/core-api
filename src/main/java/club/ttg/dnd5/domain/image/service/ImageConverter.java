package club.ttg.dnd5.domain.image.service;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

public final class ImageConverter
{
    private static final int SIGNATURE_SCAN_LIMIT = 32;

    private ImageConverter()
    {
    }

    public static ConvertedImage toWebp(byte[] inputBytes,
                                        String contentType,
                                        String originalFilename,
                                        WebpOptions options)
            throws IOException
    {
        Objects.requireNonNull(inputBytes, "inputBytes");
        if (inputBytes.length == 0)
        {
            throw new IllegalArgumentException("inputBytes is empty");
        }

        WebpOptions effectiveOptions = options != null ? options : WebpOptions.defaultLossy();

        SourceFormat sourceFormat = detectSourceFormat(inputBytes, contentType, originalFilename);
        if (sourceFormat == SourceFormat.UNKNOWN)
        {
            throw new IllegalArgumentException("Unsupported image format (only JPEG/PNG/BMP are allowed)");
        }

        BufferedImage src = readSingleImage(inputBytes);

        BufferedImage normalized = normalize(src, effectiveOptions);

        byte[] webp = writeWebp(normalized, effectiveOptions);

        return new ConvertedImage(webp, "image/webp", normalized.getWidth(), normalized.getHeight(), sourceFormat);
    }

    private static BufferedImage readSingleImage(byte[] inputBytes) throws IOException
    {
        try (ImageInputStream iis = ImageIO.createImageInputStream(new ByteArrayInputStream(inputBytes)))
        {
            if (iis == null)
            {
                throw new IOException("Unable to create ImageInputStream");
            }

            Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
            if (!readers.hasNext())
            {
                throw new IOException("No ImageIO reader found for input image");
            }

            ImageReader reader = readers.next();
            reader.setInput(iis, true, true);

            try
            {
                return reader.read(0);
            }
            finally
            {
                reader.dispose();
            }
        }
    }

    private static BufferedImage normalize(BufferedImage src, WebpOptions options)
    {
        BufferedImage image = src;

        // Ensure sRGB
        if (image.getColorModel() != null
                && image.getColorModel().getColorSpace() != null
                && image.getColorModel().getColorSpace().getType() != ColorSpace.TYPE_RGB)
        {
            BufferedImage converted = new BufferedImage(image.getWidth(), image.getHeight(),
                    image.getColorModel().hasAlpha() ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB);

            ColorConvertOp op = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_sRGB), null);
            op.filter(image, converted);
            image = converted;
        }

        // Resize to fit (optional)
        if (options.maxWidth != null || options.maxHeight != null)
        {
            int w = image.getWidth();
            int h = image.getHeight();

            int targetW = w;
            int targetH = h;

            if (options.maxWidth != null && targetW > options.maxWidth)
            {
                double k = options.maxWidth / (double) targetW;
                targetW = options.maxWidth;
                targetH = (int) Math.round(targetH * k);
            }

            if (options.maxHeight != null && targetH > options.maxHeight)
            {
                double k = options.maxHeight / (double) targetH;
                targetH = options.maxHeight;
                targetW = (int) Math.round(targetW * k);
            }

            if (targetW != w || targetH != h)
            {
                image = resize(image, targetW, targetH, options.highQualityResize);
            }
        }

        // Drop alpha if requested
        if (!options.preserveAlpha && image.getColorModel().hasAlpha())
        {
            BufferedImage rgb = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g = rgb.createGraphics();
            try
            {
                g.drawImage(image, 0, 0, null);
            }
            finally
            {
                g.dispose();
            }
            image = rgb;
        }

        return image;
    }

    private static BufferedImage resize(BufferedImage src, int targetW, int targetH, boolean highQuality)
    {
        int type = src.getColorModel().hasAlpha() ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;

        BufferedImage dst = new BufferedImage(targetW, targetH, type);
        Graphics2D g = dst.createGraphics();
        try
        {
            if (highQuality)
            {
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            }
            g.drawImage(src, 0, 0, targetW, targetH, null);
        }
        finally
        {
            g.dispose();
        }
        return dst;
    }

    private static byte[] writeWebp(BufferedImage image, WebpOptions options) throws IOException
    {
        ImageWriter writer = findWebpWriter();

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ImageOutputStream ios = ImageIO.createImageOutputStream(baos))
        {
            writer.setOutput(ios);

            ImageWriteParam param = writer.getDefaultWriteParam();

            if (param.canWriteCompressed())
            {
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);

                String losslessType = findCompressionType(param, "lossless");
                String lossyType = findCompressionType(param, "lossy");

                if (options.lossless)
                {
                    if (losslessType != null)
                    {
                        param.setCompressionType(losslessType);
                    }
                    param.setCompressionQuality(1.0f);
                }
                else
                {
                    if (lossyType != null)
                    {
                        param.setCompressionType(lossyType);
                    }
                    param.setCompressionQuality(clamp01(options.quality));
                }
            }

            writer.write(null, new IIOImage(image, null, null), param);
            ios.flush();
            return baos.toByteArray();
        }
        finally
        {
            writer.dispose();
        }
    }

    private static String findCompressionType(ImageWriteParam param, String containsLower)
    {
        String[] types = param.getCompressionTypes();
        if (types == null)
        {
            return null;
        }
        for (String t : types)
        {
            if (t != null && t.toLowerCase(Locale.ROOT).contains(containsLower))
            {
                return t;
            }
        }
        return null;
    }

    private static ImageWriter findWebpWriter()
    {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByMIMEType("image/webp");
        if (!writers.hasNext())
        {
            writers = ImageIO.getImageWritersByFormatName("webp");
        }
        if (!writers.hasNext())
        {
            throw new IllegalStateException("No WebP ImageWriter found. Add com.twelvemonkeys.imageio:imageio-webp.");
        }
        return writers.next();
    }

    private static float clamp01(float v)
    {
        if (v < 0.0f)
        {
            return 0.0f;
        }
        return Math.min(v, 1.0f);
    }

    private static SourceFormat detectSourceFormat(byte[] bytes, String contentType, String filename)
    {
        if (contentType != null && !contentType.isBlank())
        {
            String ct = contentType.toLowerCase(Locale.ROOT).trim();
            Optional<SourceFormat> byCt = SourceFormat.fromContentType(ct);
            if (byCt.isPresent())
            {
                return byCt.get();
            }
        }

        if (filename != null && !filename.isBlank())
        {
            String lower = filename.toLowerCase(Locale.ROOT);
            int dot = lower.lastIndexOf('.');
            if (dot >= 0 && dot + 1 < lower.length())
            {
                String ext = lower.substring(dot + 1);
                Optional<SourceFormat> byExt = SourceFormat.fromExtension(ext);
                if (byExt.isPresent())
                {
                    return byExt.get();
                }
            }
        }

        return SourceFormat.fromSignature(bytes).orElse(SourceFormat.UNKNOWN);
    }

    public record ConvertedImage(byte[] bytes, String contentType, int width, int height, SourceFormat sourceFormat)
    {
    }

    public static final class WebpOptions
    {
        private final boolean lossless;
        private final float quality; // 0..1
        private final boolean preserveAlpha;
        private final Integer maxWidth;
        private final Integer maxHeight;
        private final boolean highQualityResize;

        private WebpOptions(boolean lossless,
                            float quality,
                            boolean preserveAlpha,
                            Integer maxWidth,
                            Integer maxHeight,
                            boolean highQualityResize)
        {
            this.lossless = lossless;
            this.quality = quality;
            this.preserveAlpha = preserveAlpha;
            this.maxWidth = maxWidth;
            this.maxHeight = maxHeight;
            this.highQualityResize = highQualityResize;
        }

        public static WebpOptions defaultLossy()
        {
            return new WebpOptions(false, 0.82f, true, null, null, true);
        }

        public static WebpOptions lossy(float quality)
        {
            return new WebpOptions(false, quality, true, null, null, true);
        }

        public static WebpOptions lossless()
        {
            return new WebpOptions(true, 1.0f, true, null, null, true);
        }

        public WebpOptions withoutAlpha()
        {
            return new WebpOptions(lossless, quality, false, maxWidth, maxHeight, highQualityResize);
        }

        public WebpOptions resizeToFit(int maxWidth, int maxHeight)
        {
            return new WebpOptions(lossless, quality, preserveAlpha, maxWidth, maxHeight, highQualityResize);
        }

        public WebpOptions fastResize()
        {
            return new WebpOptions(lossless, quality, preserveAlpha, maxWidth, maxHeight, false);
        }
    }

    public enum SourceFormat
    {
        JPEG("image/jpeg", new String[]{"jpg", "jpeg"}),
        PNG("image/png", new String[]{"png"}),
        BMP("image/bmp", new String[]{"bmp"}),
        WEBP("image/webp", new String[]{"webp"}),
        UNKNOWN(null, new String[0]);

        private final String contentType;
        private final String[] extensions;

        SourceFormat(String contentType, String[] extensions)
        {
            this.contentType = contentType;
            this.extensions = extensions;
        }

        public static Optional<SourceFormat> fromContentType(String contentType)
        {
            for (SourceFormat f : values())
            {
                if (f.contentType != null && f.contentType.equalsIgnoreCase(contentType))
                {
                    return Optional.of(f);
                }
            }
            return Optional.empty();
        }

        public static Optional<SourceFormat> fromExtension(String ext)
        {
            for (SourceFormat f : values())
            {
                for (String e : f.extensions)
                {
                    if (e.equalsIgnoreCase(ext))
                    {
                        return Optional.of(f);
                    }
                }
            }
            return Optional.empty();
        }

        public static Optional<SourceFormat> fromSignature(byte[] bytes)
        {
            int n = Math.min(bytes.length, SIGNATURE_SCAN_LIMIT);

            // JPEG: FF D8 FF
            if (n >= 3 && (bytes[0] & 0xFF) == 0xFF && (bytes[1] & 0xFF) == 0xD8 && (bytes[2] & 0xFF) == 0xFF)
            {
                return Optional.of(JPEG);
            }

            // PNG: 89 50 4E 47 0D 0A 1A 0A
            if (n >= 8
                    && (bytes[0] & 0xFF) == 0x89
                    && bytes[1] == 0x50
                    && bytes[2] == 0x4E
                    && bytes[3] == 0x47
                    && (bytes[4] & 0xFF) == 0x0D
                    && (bytes[5] & 0xFF) == 0x0A
                    && (bytes[6] & 0xFF) == 0x1A
                    && (bytes[7] & 0xFF) == 0x0A)
            {
                return Optional.of(PNG);
            }

            // BMP: "BM"
            if (n >= 2 && bytes[0] == 'B' && bytes[1] == 'M')
            {
                return Optional.of(BMP);
            }

            // WEBP: "RIFF" .... "WEBP"
            if (n >= 12
                    && bytes[0] == 'R'
                    && bytes[1] == 'I'
                    && bytes[2] == 'F'
                    && bytes[3] == 'F'
                    && bytes[8] == 'W'
                    && bytes[9] == 'E'
                    && bytes[10] == 'B'
                    && bytes[11] == 'P')
            {
                return Optional.of(WEBP);
            }

            return Optional.empty();
        }
    }
}
