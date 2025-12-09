package src;

import java.awt.Point;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Spurs {

private static Point furthestFromCenter(List<Point> points, Point center) {
    Point best = null;
    double maxDistance = -1;

    for (Point point : points) {
        double distance = Mountain.findDistance(center, point);
        if (distance > maxDistance) {
            maxDistance = distance;
            best = point;
        }
    }
    return best;
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

        Point point = furthestFromCenter(outline, center);
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


    public static void main(String[] args) {
        File path = new File("images/photo.png");
        Boolean[][] mask = Outline.normalizeImage(path);
        Point[] positions = locationsForSpurs(mask, 3);
        System.out.println(Arrays.toString(positions));
    }
}
