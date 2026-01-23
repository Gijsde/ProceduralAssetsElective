import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import javax.imageio.ImageIO;

public class ImageRW {

    public static boolean[] LoadImageToMask(BufferedImage image, int height, int width) {

        boolean[] mask = new boolean[width*height];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                mask[y * width + x] = ((image.getRGB(x, y))&0xFF) < 128;
            }
        }
        System.out.println("Created the binary mask.");
        return mask;
    }

    public static Properties loadProperties(String file) throws IOException {
        Properties props = new Properties();

        try (FileInputStream fis = new FileInputStream("config.properties")) {
            props.load(fis);
        }
        return props;
    }

    public static void writeBooleanMaskToFile(boolean[] mask, File file, int height, int width) {

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

        for (int y = 0; y < width; y++) {
            for (int x = 0; x < height; x++) {
                // int rgb = 255;
                // if (mask[y * width + x]) rgb = 0;
                int rgb = (mask[y * width + x] ? 0 : 0xffffff);
                image.setRGB(x, y, rgb);
            }
        }

        try {
            ImageIO.write(image, "png", file);
        } catch (IOException e) {
            System.out.println("image could not be written");
        }
        System.out.println("Wrote the file to: " + file);
    }

    public static void writeIntMaskToFile(int[] mask, File file, int height, int width) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);


        for (int y = 0; y < width; y++) {
            for (int x = 0; x < height; x++) {
                // int rgb = 255;
                // if (mask[y * width + x]) rgb = 0;
                int color = mask[y * width + x];
                int rgb = color << 16 | color << 8 | color;
                image.setRGB(x, y, rgb);
            }
        }
        try {
            ImageIO.write(image, "png", file);
        } catch (IOException e) {
            System.out.println("image could not be written");
        }
        System.out.println("Wrote the file to: " + file);
    }
}