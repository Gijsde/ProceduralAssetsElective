package src;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class Mountain {

    public static double findDistance(Point from, Point to) {
        return Math.sqrt(Math.pow((from.x - to.x), 2) + Math.pow((from.y - to.y), 2));
    }

    public static Point[] findOutlinePoints(Boolean[][] mask) {
        List<Point> points = new ArrayList<>();

        for (int y = 0; y < mask.length; y++) {
            for (int x = 0; x < mask[y].length; x++) {
                if (Boolean.TRUE.equals(mask[y][x])) {
                    points.add(new Point(x, y));
                }
            }
        }
        return points.toArray(new Point[0]);
    }


    public static int[][] fillOutline(Boolean[][] mask) {
        int height = mask.length;
        int width = mask[0].length;
    
        int[][] heightMap = new int[height][width];
        Point[] outline = findOutlinePoints(mask);
        Point center = Outline.findCenterObject(mask);
    
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
    
                if (mask[y][x] == null || Boolean.TRUE.equals(mask[y][x])) {
                    heightMap[y][x] = 255;
                } else if (center.equals(new Point(x,y))) {
                    heightMap[y][x] = 0;
                } else {
                    double distanceToLine = Double.MAX_VALUE;

                    // Current point as local reusable object
                    Point current = new Point(x, y);
    
                    // Find minimum distance to any outline point
                    for (Point p : outline) {
                        double distance = findDistance(current, p);
                        if (distance < distanceToLine) {
                            distanceToLine = distance;
                        }
                    }
    
                    double distanceToCenter = findDistance(current, center);
                    double totalDistance = distanceToCenter + distanceToLine;
    
                     
                    // double ratio = distanceToCenter / totalDistance;
                    double ratio = Math.sqrt(distanceToCenter) / Math.sqrt(totalDistance);
                    int value = (int) Math.round(255 * ratio);
                    heightMap[y][x] = value;                
                }                
            }
        }
    
        return heightMap;
    }

    public static BufferedImage toGrayscaleImage(int[][] data) {
        int height = data.length;
        int width = data[0].length;
    
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
    
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Convert signed byte (-128..127) to unsigned (0..255)
                int value = data[y][x];
                int rgb = (value << 16) | (value << 8) | value; // convert to RGB format

                img.setRGB(x, y, rgb);
            }
        }
    
        return img;
    }




    


    public static void main(String[] args) {
        File path = new File("images/photo.png");
        Boolean[][] mask = Outline.normalizeImage(path);
        int[][] heightMap = fillOutline(mask);
        System.out.println("no errors so far!!");

        File output = new File("images/mountain2.png");

        ImageRW.saveGreyscaleMaskToFile(heightMap, output);


        /**
         * TODO: add a way to force the lines mountains have
         * this will also create valleys.
         * IDEA: user inputs a number this will be the amount of lines the mountain will have
         * split the mountain up in x amount of parts. look for the farthest point in those areas
         * from the center to those farthest points will be the lines
         */

        /**
         * TODO: allow for creating the lines a mountain has independantly from the filling in of the mountain.
         * the lines that the mountain has should be seen as a peak but with the respective height as the max height.
         * the function for filling in the rest of the mountain should use the lines to determine the height of each pixel
         */

        /**
         * TODO: allow for multiple different kind of functions to be used to determine the height of the mountain.
         * f.e. be able to choose between a Sigmoid function or a linear function
         */

        /**
         * TODO: add perlin noise to the height of the mountain
         * this will create some roughness to the mountain meaning it wont be as smooth.
         */


    }
}
