package src;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class ImageRW {

    public static void saveGreyscaleMaskToFile(int[][] mask, File file) {
        int height = mask.length;
        int width = mask[0].length;
    
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        
        //set pixels to the correct greyscale value
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int value = mask[y][x];
                int rgb = (value << 16) | (value << 8) | value; // convert to RGB format

                img.setRGB(x, y, rgb);
            }
        }

        try {
            ImageIO.write(img, "png", file);
        } catch (IOException e) {
            System.out.println("image could not be written");
        }
    }

    
    public static boolean[][] loadFileAsBlackWhiteMask(File file) {
        try {
            BufferedImage image = ImageIO.read(file);
            int width = image.getWidth();
            int height = image.getHeight();

            boolean[][] mask = new boolean[height][width];

            for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        int blue = (image.getRGB(x, y))&0xFF; // takes the blue value of the pixel
                        // sets the boolean at location to 1 if it is blacker than it is white
                        mask[y][x] = blue < 128;
                    }
            }
            return mask;
        } catch (IOException e) {
            System.out.println("image could not be read");
        } 
        return null;
    }
}
