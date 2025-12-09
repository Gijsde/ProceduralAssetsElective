package src;

import java.awt.Point;
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

    private static void addSpurToHeightMap(int[][] heightMap, boolean[][] spurMask, Point[] outline, Point center) {
        for (int y = 0; y < spurMask.length; y++) {
            for (int x = 0; x < spurMask[0].length; x++) {
                if (spurMask[y][x]) {
                    double distanceToCenter = findDistance(center, new Point(x,y));
                    double totalDistance = Helper.smallestDistanceFromPoint(outline, new Point(x, y)) + distanceToCenter;
    
                    double ratio = distanceToCenter/totalDistance;
                    int value = (int) Math.round(ratio * 255);
                    heightMap[y][x] = value;
                } else heightMap[y][x] = 255;
            }
        }
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

    // public static void fillOutline2(Boolean[][] mask, int[][] heightMap, Point[] spur, Point[] outline, Point center) {
    //     int height = mask.length;
    //     int width = mask[0].length;

    //     for (int y = 0; y < height; y++) {
    //         for (int x = 0; x < width; x++) {
    //             //ensures outline and outside is always white
    //             if (mask[y][x] == null || Boolean.TRUE.equals(mask[y][x])) {
    //                 heightMap[y][x] = 255;
    //             } else if (heightMap[y][x] == 255){
    //                 Point point = new Point(x, y);
    //                 Point spurPoint = Helper.closestFromPoint(spur, point);
    //                 double distanceToCenter = findDistance(spurPoint, point);
    //                 double distanceToLine = Helper.smallestDistanceFromPoint(outline, point);
    //                 double totalDistance = distanceToCenter + distanceToLine;
                    
                    
                    
    //                 double ratio = Math.sqrt(distanceToCenter) / Math.sqrt(totalDistance);
    //                 int value = (int) Math.round(255 * ratio);
    //                 System.out.println("setting: " + value + " on: " + new Point(x, y));
    //                 heightMap[y][x] = value;  
    //             }
    //         }
    //     }
    // }
    public static void fillOutline2(Boolean[][] mask, int[][] heightMap, Point[] spur, Point[] outline, Point center) {
        int height = mask.length;
        int width = mask[0].length;
    
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // ensure outline and outside is always white
                if (mask[y][x] == null || Boolean.TRUE.equals(mask[y][x])) {
                    heightMap[y][x] = 255;
                } else {
                    Point point = new Point(x, y);
    
                    // find closest spur point
                    Point spurPoint = Helper.closestFromPoint(spur, point);
    
                    // get the height at that spur point
                    int spurHeight = heightMap[spurPoint.y][spurPoint.x];
    
                    // distances from current point
                    double distanceToCenter = findDistance(spurPoint, point);
                    double distanceToLine = Helper.smallestDistanceFromPoint(outline, point);
                    double totalDistance = distanceToCenter + distanceToLine;
                    if (totalDistance == 0) totalDistance = 1e-6;
    
                    // ratio (0 at spur, 1 at line)
                    double ratio = Math.sqrt(distanceToCenter / totalDistance);
    
                    // final value interpolated from spur height
                    int value = (int) Math.round(spurHeight * (1 - ratio) + 255 * ratio);
    
                    heightMap[y][x] = value;
                }
            }
        }
    }


    public static void main(String[] args) {
        File path = new File("images/art.png");
        Boolean[][] mask = Outline.normalizeImage(path);
        int[][] heightMap = new int[mask.length][mask[0].length];
        // int[][] heightMap = fillOutline(mask);
        System.out.println("no errors so far!!");
        boolean[][] spurMask = Spurs.makeSpurMask(mask, 4);
        Point center = Outline.findCenterObject(mask);
        Point[] spur = Helper.findAllPoints(spurMask);
        Point[] outline = Helper.findAllPoints(mask);

        for (int y = 0; y < mask.length; y++) {
            System.out.print(y);
            for (int x = 0; x < mask[0].length; x++) {
                if (Boolean.TRUE.equals(mask[y][x])) {
                    System.out.print("x");
                } else if (Boolean.FALSE.equals(mask[y][x])) {
                    System.out.print(".");
                } else System.out.print(" ");
            }
            System.out.println(" ");
        }


        // for (int y = 0; y < heightMap.length; y++) {
        //     System.out.print(y);
        //     for (int x = 0; x < heightMap[0].length; x++) {
        //         System.out.print(heightMap[y][x]);
        //     }
        //     System.out.println(" ");
        // }

        addSpurToHeightMap(heightMap, spurMask, outline, center);
        fillOutline2(mask, heightMap, spur, outline, center);
        System.out.println(heightMap[16][20]);

        System.out.println("saving file");

        File output = new File("images/mountain3.png");

        ImageRW.saveGreyscaleMaskToFile(heightMap, output);


        /**
         * TODO: allow for multiple different kind of functions to be used to determine the height of the mountain.
         * f.e. be able to choose between a Sigmoid function or a linear function
         */

        /**
         * TODO: add perlin noise to the height of the mountain
         * this will create some roughness to the mountain meaning it wont be as smooth.
         */

        /**
         * TODO: add some divergent to the Spurs
         * this will allow the mountain to look more natural, as it isn't as "perfect" as without.
         */
    }
}
