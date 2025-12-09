package src;

import java.awt.Point;
import java.io.File;
import java.util.Arrays;

public class Spurs {

    private static Point farthestFromCenter(Boolean[][] mask) {
        Point[] points = Mountain.findOutlinePoints(mask);
        Point center = Outline.findCenterObject(mask);

        double distance = Double.MIN_VALUE;
        Point farthesPoint = null;
        for (Point point : points) {
            if (Mountain.findDistance(point, center) > distance) {
                distance = Mountain.findDistance(point, center);
                farthesPoint = point;
            }
        }
        return farthesPoint;
    }

    private static double angleWithCenter(Point other, Point center) {
        double dx = other.x - center.x;
        double dy = other.y - center.y;
        return Math.atan2(dy, dx);
    }

    private static double[] anglesForSpurs(Boolean[][] mask, int count) {
        Point furthest = farthestFromCenter(mask);
        Point center = Outline.findCenterObject(mask);
        double rad = angleWithCenter(furthest, center);
        double[] rads = new double[count];

        for (int i = 0; i < count; i++) {
            rads[i] = i*Math.TAU/count + rad;
        }
        return rads;
    }

    private static double angleDiff(double a, double b) {
        return Math.abs(Math.atan2(Math.sin(a - b), Math.cos(a - b)));
    }
    
    
    private static int findClosestIndex(double[] arr, double target) {
        int closestIndex = 0;
        double minDiff = Math.abs(arr[0] - target);
    
        for (int i = 1; i < arr.length; i++) {
            double diff = angleDiff(arr[i], target);
            if (diff < minDiff) {
                minDiff = diff;
                closestIndex = i;
            }
        }
        return closestIndex;
    }

    private static Point[] locationForSpurs (Boolean[][] mask, int count) {
        double[] angles = anglesForSpurs(mask, count);
        Point[] farthestPoints = new Point[count];
        double[] distances = new double[count];
        Point center = Outline.findCenterObject(mask);

        for (int y = 0; y < mask.length; y++) {
            for (int x = 0; x < mask[0].length; x++) {
                if (Boolean.TRUE.equals(mask[y][x])) {
                    Point point = new Point(x, y);
                    int closestIndex = findClosestIndex(angles, angleWithCenter(point, center));
                    double distance = Mountain.findDistance(point, center);
                    if (distances[closestIndex] < distance) {
                        farthestPoints[closestIndex] = point;
                        distances[closestIndex] = distance;
                    }   
                }
                
            }   
        }
        return farthestPoints;
    }
    




    public static void main(String[] args) {
        File path = new File("images/photo.png");
        Boolean[][] mask = Outline.normalizeImage(path);
        System.out.println(Arrays.toString(locationForSpurs(mask, 3)));        
    }
    
}
