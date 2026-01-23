import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Heightmap {

    public static int[] applySpurs(List<Integer> spurs, List<Integer> outline, int height, int width, int center, int function) {
        int size = width * height;
        int[] heightmap = new int[size];

        // Fast membership test for spurs
        BitSet spurMask = new BitSet(size);
        for (int s : spurs) {
            if (s >= 0 && s < size) spurMask.set(s);
        }

        int x0 = center % width;
        int y0 = center / width;

        for (int location : spurs) {
            if (location < 0 || location >= size) continue;

            int x1 = location % width;
            int y1 = location / width;

            double distCenter = Helper.distance(x0, y0, x1, y1);
            double distOutline = Helper.findClosestDistance(location, outline, width);

            double denom = distCenter + distOutline;
            double factor;
            if (denom == 0.0) {
                // both distances zero — choose neutral value (0.0) or 0.5 depending on intent
                factor = 0.0;
            } else {
                factor = 1.0 - (distCenter / denom); // equivalent to distOutline/denom
            }

            factor = Functions.applyFunction(function, factor);

            int value = (int) Math.round(factor * 255.0);
            heightmap[location] = Helper.clamp255(value);
        }
        return heightmap;
    }

     public static int[] createHeightmap(List<Integer> outline, List<Integer> spurs, int height, int width, Properties props) throws InterruptedException, ExecutionException {
        final int size = width * height;

        if (spurs == null || spurs.isEmpty()) {
            // fall back: either return zeros or compute different behavior
            return new int[size];
        }

        final int center = Helper.findCenter(outline, width);

        // First, compute spur values (single-threaded call you already had)
        final int[] heightmap = applySpurs(spurs, outline, height, width, center, Integer.parseInt(props.getProperty("spurFunction")));

        // Build a fast membership mask for spurs
        final boolean[] spurMask = new boolean[size];
        for (int s : spurs) {
            if (s >= 0 && s < size) spurMask[s] = true;
        }

        // Parallel setup
        final int nThreads = Math.max(1, Runtime.getRuntime().availableProcessors());
        ExecutorService pool = Executors.newFixedThreadPool(nThreads);
        List<Future<?>> futures = new ArrayList<>(nThreads);

        // split by rows: each task handles [startRow, endRow)
        int rowsPer = Math.max(1, height / nThreads);
        for (int t = 0; t < nThreads; t++) {
            final int startRow = t * rowsPer;
            final int endRow = (t == nThreads - 1) ? height : Math.min(height, startRow + rowsPer);

            futures.add(pool.submit(() -> {
                // local copies for speed
                final int w = width;

                for (int y = startRow; y < endRow; y++) {
                    int base = y * w;
                    for (int x = 0; x < w; x++) {
                        int i = base + x;
                        if (spurMask[i]) continue; // already set by applySpurs

                        int closestSpur = Helper.findClosest(i, spurs, w);
                        if (closestSpur < 0 || closestSpur >= size) {
                            // no valid spur: leave as is (0) or handle differently
                            continue;
                        }

                        int sx = closestSpur % w;
                        int sy = closestSpur / w;

                        double distSpur = Helper.distance(sx, sy, x, y);
                        double distOutline = Helper.findClosestDistance(i, outline, w);

                        double denom = distSpur + distOutline;
                        double factor = (denom == 0.0) ? 0.0 : 1.0 - (distSpur / denom); // = distOutline/denom

                        factor = Functions.applyFunction(Integer.parseInt(props.getProperty("heightmapFunction")), factor);

                        int value = (int) Math.round(factor * (double) heightmap[closestSpur]);
                        heightmap[i] = Helper.clamp255(value);
                    }
                }
            }));
        }

        // wait for tasks
        for (Future<?> f : futures) f.get();
        pool.shutdown();

        System.out.println("Filled in the heightmap.");
        return heightmap;
    }
}