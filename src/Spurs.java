package src;

import java.awt.Point;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Spurs {


    //made by chatGPT because I can't be bothered inplementing an algorithm that already exists.
    private static void drawLine(boolean[][] mask, Point a, Point b) {
        int x0 = a.x;
        int y0 = a.y;
        int x1 = b.x;
        int y1 = b.y;
    
        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;
    
        int err = dx - dy;
    
        while (true) {
    
            // Bounds check
            if (y0 >= 0 && y0 < mask.length &&
                x0 >= 0 && x0 < mask[0].length) {
                mask[y0][x0] = true;
            }
    
            // End reached
            if (x0 == x1 && y0 == y1)
                break;
    
            int e2 = 2 * err;
    
            if (e2 > -dy) {
                err -= dy;
                x0 += sx;
            }
            if (e2 < dx) {
                err += dx;
                y0 += sy;
            }
        }
    }

    private static double angle(Point from, Point to) {
        double dx = to.x - from.x;
        double dy = to.y - from.y;
        double angle = Math.atan2(dy, dx);

        // normalize angle to [-π, π]
        if (angle < -Math.PI) angle += 2 * Math.PI;
        if (angle >  Math.PI) angle -= 2 * Math.PI;

        return angle;
    }

    private static boolean isWithinRanges(double angle, double[] angles, double range, int filled) {
        for (int i = 0; i < filled; i++) {
            double ang = angles[i];

            // compute shortest circular distance
            double diff = Math.abs(angle - ang);
            if (diff > Math.PI) diff = 2*Math.PI - diff;

            if (diff < range) return true;
        }
        return false;
    }

    private static Point[] locationsForSpurs(Boolean[][] mask, int count) {
        Point[] outlineArr = Mountain.findOutlinePoints(mask);
        List<Point> outline = new ArrayList<>(Arrays.asList(outlineArr));

        Point[] result = new Point[count];
        double[] usedAngles = new double[count];
        int filled = 0;

        Point center = Outline.findCenterObject(mask);

        double angleRange = Math.PI / count;

        while (filled < count && !outline.isEmpty()) {

            Point point = Helper.furthestFromPoint(outline, center);
            if (point == null) break;

            double ang = angle(center, point);

            if (!isWithinRanges(ang, usedAngles, angleRange, filled)) {
                result[filled] = point;
                usedAngles[filled] = ang;
                filled++;
            }
            outline.remove(point);
        }
        return result;
    }

    public static boolean[][] makeSpurMask(Boolean[][] mask, int count) {

        // Point[] points, Point center, int height, int width
        Point[] points = locationsForSpurs(mask, count);
        Point center = Outline.findCenterObject(mask);

        boolean[][] spurMask = new boolean[mask.length][mask[0].length];
        for (Point point : points) {
            drawLine(spurMask, point, center);
        }
        return spurMask;
    }


    public static void main(String[] args) {
        File inPath = new File("images/photo.png");
        File outPath = new File("images/line.png");
        Boolean[][] mask = Outline.normalizeImage(inPath);
        boolean[][] spurMask = makeSpurMask(mask, 3);
        
        int[][] intMask = new int[mask.length][mask[0].length];
        for (int y = 0; y < mask.length; y++) {
            for (int x = 0; x < mask[0].length; x++) {
                if (Boolean.TRUE.equals(spurMask[y][x])) {
                    intMask[y][x] = 0;
                } else intMask[y][x] = 255;
            }
            
        }
        System.out.println("saving image");
        ImageRW.saveGreyscaleMaskToFile(intMask, outPath);
    }
}
