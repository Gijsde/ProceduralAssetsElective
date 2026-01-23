import java.util.ArrayList;
import java.util.List;

public class Helper {
    public static List<Integer> findBlack(boolean[] mask) {
        List<Integer> list = new ArrayList<>();

        for (int i = 0; i < mask.length; i++) {
            if (mask[i]) {
                list.add(i);
            }
        }
        return list;
    }

    public static double distance(int x0, int y0, int x1, int y1) {
        return Math.sqrt((x1-x0)*(x1-x0) + (y1-y0)*(y1-y0));
    }

    public static int findClosest(int start, List<Integer> list, int width) {
        int closest = 0;
        double minDist = Double.MAX_VALUE;

        int x0 = start % width;
        int y0 = start / width;

        for (Integer integer : list) {
            int x1 = integer % width;
            int y1 = integer / width;
            double curDistance = distance(x0, y0, x1, y1);
            if (curDistance < minDist) {
                closest = integer;
                minDist = curDistance;
            }
        }
        return closest;
    }

    public static double findClosestDistance (int start, List<Integer> list, int width) {
        double minDist = Double.MAX_VALUE;

        int x0 = start % width;
        int y0 = start / width;

        for (Integer integer : list) {
            int x1 = integer % width;
            int y1 = integer / width;
            double curDistance = distance(x0, y0, x1, y1);
            if (curDistance < minDist) {
                minDist = curDistance;
            }
        }
        return minDist;
    }

    public static int findFarthest(int start, List<Integer> list, int width) {
        int farthest = 0;
        double maxDist = Double.MIN_VALUE;

        int x0 = start % width;
        int y0 = start / width;

        for (Integer integer : list) {
            int x1 = integer % width;
            int y1 = integer / width;
            double curDistance = distance(x0, y0, x1, y1);
            if (curDistance > maxDist) {
                farthest = integer;
                maxDist = curDistance;
            }
        }
        return farthest;
    }

    public static int findCenter(List<Integer> list, int width) {
        int sumx = 0;
        int sumy = 0;
        
        for (Integer integer : list) {
            sumx += integer % width;
            sumy += integer / width;
        }
        int avgx = sumx / list.size();
        int avgy = sumy / list.size();
        return avgy * width + avgx;
    }

    public static double findAngle(int from, int to, int width) {
        int x0 = from % width;
        int y0 = from / width;
        int x1 = to % width;
        int y1 = to / width;

        return Math.atan2(y1 - y0, x1 - x0);
    }

    public static List<Integer> sortPointsByDistanceDescending(int start, List<Integer> points, int width) {
        // Calculate the starting coordinates once
        int x0 = start % width;
        int y0 = start / width;

        // Create a copy to avoid modifying the original list
        List<Integer> sortedList = new ArrayList<>(points);

        sortedList.sort((a, b) -> {
            // Coordinate for point A
            int xA = a % width;
            int yA = a / width;
            double distA = distance(x0, y0, xA, yA);

            // Coordinate for point B
            int xB = b % width;
            int yB = b / width;
            double distB = distance(x0, y0, xB, yB);

            // Compare distances (Ascending: closest to farthest)
            return Double.compare(distB, distA);
        });

        return sortedList;
    }

    public static double useFunction(double value, int function) {
        return value;
    }

    public static int clamp255(int v) {
        if (v < 0) return 0;
        if (v > 255) return 255;
        return v;
    }
}
