import java.util.ArrayList;
import java.util.List;

public class Spurs {   

    private static boolean isWithinRanges(double angle, List<Double> angles, double dAngle) {
        
        for (Double target : angles) {
            double diff = angle - target;

            if (diff > Math.PI) {
                diff -= 2 * Math.PI;
            } else if (diff < -Math.PI) {
                diff += 2 * Math.PI;
            }
            if (Math.abs(diff) <= dAngle) return true;
        }
        return false;
    }

    private static void drawLine(boolean[] mask, int width, int a, int b) {

        int x0 = a % width;
        int y0 = a / width;

        int x1 = b % width;
        int y1 = b / width;

        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;

        int err = dx - dy;

        int height = mask.length / width;

        while (true) {

            // Bounds check
            if (x0 >= 0 && x0 < width &&
                y0 >= 0 && y0 < height) {

                mask[y0 * width + x0] = true;
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

    private static boolean[] drawLines(int height, int width, List<Integer> locations, int from) {
        boolean[] mask = new boolean[height*width];

        for (Integer location : locations) {
            drawLine(mask, width, location, from);
        }
        return mask;
    }

    public static List<Integer> createSpurs(List<Integer> outline, int height, int width, int count) {

        // collect locations in a dynamic list so we don't see leftover zeros
        int center = Helper.findCenter(outline, width);
        
        outline = Helper.sortPointsByDistanceDescending(center, outline, width);
        
        double dAngle = Math.PI / count; // use 2*Math.PI for portability
        List<Double> angles = new ArrayList<>();
        List<Integer> locationsList = new ArrayList<>();

        for (Integer pt : outline) {
            if (angles.size() >= count) break;
            
            double angle = Helper.findAngle(center, pt, width);

            // normalize here if findAngle returns -pi..pi it still works because isWithinRanges normalizes
            if (!isWithinRanges(angle, angles, dAngle)) {
                angles.add(angle);
                locationsList.add(pt);
            }
        }

        boolean[] spurs = drawLines(height, width, locationsList, center);

        System.out.println("Created the spurs");
        return Helper.findBlack(spurs);
    }
    
}
