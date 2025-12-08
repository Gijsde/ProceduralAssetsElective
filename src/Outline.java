package src;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Queue;
import java.awt.Point;
import java.util.Arrays;


public class Outline {
    public static int[][] directions = {
        {1, 0},
        {-1, 0},
        {0, 1},
        {0, -1}
    };

    // these are all the cases where a pixel is not allowed to be removed, rotation not included
    public static boolean[][] blacklist = {
        {true, false, false, false, true, false, true, false, false},
        {true, false, false, false, true, false, false, true, false},
        {true, false, false, false, true, false, false, false, true},
        {true, false, false, false, true, true, false, false, false},
        {true, false, false, true, true, false, false, false, true},
        {true, false, false, true, true, true, false, false, false},
        {true, false, true, true, true, false, false, false ,false},
        {true, false, true, false, true, true, false, false ,false},
        {true, true, false, false, true, false, false,false, true},
        {false, false, false, true, true, true, false, false, false},
        {true, false, false, true, true, false, false, false, false},
        {true, true, false, false, true, false, false, false, false},
        {true, true, false, false, true, false, false, true, false}
    };

    public static boolean[][] createBlackWhiteMask(BufferedImage image){
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
    }

    public static boolean[][] createSinglePixelLine(boolean[][] mask, Point center, int maxHeight, int maxWidth) {
        System.out.println("working on single pixel line now");
        boolean hasremoved = true;

        while (hasremoved) {
            hasremoved = false;
            Queue<Point> queue = new ArrayDeque<>();
            boolean[][] visited = new boolean[maxHeight][maxWidth];
            queue.add(center);
            visited[center.y][center.x] = true;

            while (!queue.isEmpty()) {
                Point current = queue.poll();
                // the program only has to check location with a true value
                if (mask[current.y][current.x]) {
                    // save the position and its surroundings
                    boolean[] pos = takePart(mask, maxHeight, maxWidth, current);
                    
                    if (isRemovable(pos)) {
                        hasremoved = true;
                        mask[current.y][current.x] = false;
                    }
                } 

                for (int[] dir : directions) {
                    int futureX = current.x + dir[0];
                    int futureY = current.y + dir[1];
                    if (futureX >= 0 && futureX < maxWidth && futureY >= 0 && futureY < maxHeight) {
                        if (!visited[futureY][futureX]) {
                            queue.add(new Point(futureX, futureY));
                            visited[futureY][futureX] = true;
                        }
                    }
                }
            }
        }
        return mask;
    }

    public static boolean isRemovable(boolean[] pos) {
        
        for (int index = 0; index < 4; index++) {
            pos = rotateClockwise(pos);
            for (boolean[] entry : blacklist) {
                if (Arrays.equals(entry, pos)) return false;
            }
        }
        return true;
    }

    // takes a 3x3 square around a Point, a locations is out of bounds it sets that location to false.
    public static boolean[] takePart(boolean[][] map, int maxHeight, int maxWidth, Point current) {
        boolean[] pos = new boolean[9];
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
        
                int nx = current.x + dx;
                int ny = current.y + dy;
        
                int idx = (dy + 1) * 3 + (dx + 1);
        
                // Bounds check
                if (nx < 0 || ny < 0 || nx >= maxWidth || ny >= maxHeight) {
                    pos[idx] = false;
                } else {
                    pos[idx] = map[ny][nx];
                }
            }
        }
        return pos;
    }

    // only works with an array of size 9 if the array is seen as a 3x3 grid read from left to right, top to bottom
    // idealy would be writtern more robust but I could care less
    public static boolean[] rotateClockwise(boolean[] pos) {
        boolean[] rotated = new boolean[9];
        rotated[0] = pos[6];
        rotated[1] = pos[3];
        rotated[2] = pos[0];
        rotated[3] = pos[7];
        rotated[4] = pos[4];
        rotated[5] = pos[1];
        rotated[6] = pos[8];
        rotated[7] = pos[5];
        rotated[8] = pos[2];
        return rotated;
    }

    public static boolean[][] shaving(boolean[][] originalMask) {

        int height = originalMask.length;
        int width = originalMask[0].length;
    
        int minX = width - 1;
        int minY = height - 1;
        int maxX = 0;
        int maxY = 0;
    
        // find bounding box
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (originalMask[y][x]) {
                    if (y < minY) minY = y;
                    if (y > maxY) maxY = y;
                    if (x < minX) minX = x;
                    if (x > maxX) maxX = x;
                }
            }
        }
    
        int newH = (maxY - minY) + 1;
        int newW = (maxX - minX) + 1;
    
        boolean[][] image = new boolean[newH][newW];
    
        // copy cropped area
        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                image[y - minY][x - minX] = originalMask[y][x];
            }
        }
    
        return image;
    }

    public static boolean[][] normalizeImage(File file) {
        try {
            BufferedImage image = ImageIO.read(new File("images/photo.png"));
            boolean[][] mask = createBlackWhiteMask(image);
            int height = image.getHeight();
            int width = image.getWidth();

            createSinglePixelLine(mask, new Point(width/2, height/2), height, width);
            boolean[][] smallerMask = shaving(mask);

            return smallerMask;
        } catch(IOException e) {
            System.out.println("image could not be loaded");
        }
        return null;
    }


    public static void main(String[] args) {

        File path = new File("images/photo.png");
        boolean[][] mask = normalizeImage(path);

        
        // Load image
        int height = mask.length;
        int width = mask[0].length;

        for (int y = 0; y < height; y++) {
            System.out.print(y + " ");
            for (int x = 0; x < width; x++) {
                if (x == width/2 && y == height/2) {
                    System.out.print("o");
                } else {
                    System.out.print(mask[y][x] ? "#" : ".");
                }
            }
            System.out.println();
        }
    }
}
