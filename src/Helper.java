package src;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class Helper {

    public static Point furthestFromPoint(List<Point> points, Point point) {
        Point best = null;
        double maxDistance = -1;

        for (Point element : points) {
            double distance = Mountain.findDistance(point, element);
            if (distance > maxDistance) {
                maxDistance = distance;
                best = element;
            }
        }
        return best;
    }

    public static Point closestFromPoint(Point[] points, Point point) {
        Point best = null;
        double minDistance = Double.MAX_VALUE;

        for (Point element : points) {
            double distance = Mountain.findDistance(point, element);
            if (distance < minDistance) {
                minDistance = distance;
                best = element;
            }
        }
        return best;
    }

    public static Double smallestDistanceFromPoint(Point[] points, Point point) {
        double minDistance = Double.MAX_VALUE;

        for (Point element : points) {
            double distance = Mountain.findDistance(point, element);
            if (distance < minDistance) {
                minDistance = distance;
            }
        }
        return minDistance;
    }

    public static Point[] findAllPoints(boolean[][] mask) {
        ArrayList<Point> locations = new ArrayList<>();
        for (int y = 0; y < mask.length; y++) {
            for (int x = 0; x < mask[0].length; x++) {
                if (mask[y][x]) {
                    locations.add(new Point(x, y));
                } 
            }
        }
        return locations.toArray(Point[]::new);
    }
    public static Point[] findAllPoints(Boolean[][] mask) {
        ArrayList<Point> locations = new ArrayList<>();
        for (int y = 0; y < mask.length; y++) {
            for (int x = 0; x < mask[0].length; x++) {
                if (Boolean.TRUE.equals(mask[y][x])) {
                    locations.add(new Point(x, y));
                } 
            }
        }
        return locations.toArray(Point[]::new);
    }

    public static double useFunctionForRatio(int function, double ratio) {
        
        switch (function) {
            case 1 -> {
                return ratio;
            }
            case 2 -> {
                return Math.sqrt(ratio);
            }
            case 3 -> {
                
                return (3 * Math.pow(ratio, 2)) - (2 * (Math.pow(ratio, 3)));
            }
            case 4 -> {
                return Math.pow(ratio, 4);
            }
        }
        
        return 0.0;
    }
}
