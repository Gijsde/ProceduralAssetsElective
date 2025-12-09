package src;

import java.awt.Point;
import java.io.File;

public class Spurs {

    private static double farthestFromCenter(Boolean[][] mask) {
        Point[] points = Mountain.findOutlinePoints(mask);
        Point center = Outline.findCenterObject(mask);

        double distance = Double.MIN_VALUE;
        for (Point point : points) {
            if (Mountain.findDistance(point, center) > distance) {
                distance = Mountain.findDistance(center, point);
            }
        }

        return .0;
    }
    




    public static void main(String[] args) {
        File path = new File("images/photo.png");
        Boolean[][] mask = Outline.normalizeImage(path);
        // double distance = farthestFromCenter(mask);
        // System.out.println(distance);
        
    }
    
}
