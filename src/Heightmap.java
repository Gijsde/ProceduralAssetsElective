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
                // both distances zero — choose neutral value (0.0)
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

        // parse function IDs once
        final int spurFunction = Integer.parseInt(props.getProperty("spurFunction", "0"));
        final int heightmapFunction = Integer.parseInt(props.getProperty("heightmapFunction", "0"));

        // First, compute spur values (single-threaded call you already had)
        final int[] heightmap = applySpurs(spurs, outline, height, width, center, spurFunction);

        // Build a fast membership mask for spurs
        final boolean[] spurMask = new boolean[size];
        for (int s : spurs) {
            if (s >= 0 && s < size) spurMask[s] = true;
        }

        // Precompute spur coordinates to avoid repeated div/mod
        final int nSpurs = spurs.size();
        final int[] spurIdx = new int[nSpurs];
        final int[] spurX = new int[nSpurs];
        final int[] spurY = new int[nSpurs];
        for (int i = 0; i < nSpurs; i++) {
            int idx = spurs.get(i);
            spurIdx[i] = idx;
            spurX[i] = idx % width;
            spurY[i] = idx / width;
        }

        // Parameters for smoothing
        final int K = Math.min(20, nSpurs); // number of nearest spurs to blend (tuneable: 2..4)
        final double IDW_POWER = 10.0;     // inverse-distance weighting power (tuneable)
        final double EPS = 1e-6;          // small epsilon to avoid div-by-zero

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
                final int hmFunc = heightmapFunction;

                // temporary arrays for K-NN selection
                final double[] knnDist = new double[K];
                final int[] knnIdx = new int[K];

                for (int y = startRow; y < endRow; y++) {
                    int base = y * w;
                    for (int x = 0; x < w; x++) {
                        int i = base + x;
                        if (spurMask[i]) continue; // already set by applySpurs

                        // find outline distance once per pixel
                        double distOutline = Helper.findClosestDistance(i, outline, w);

                        // find K nearest spurs by linear scan (replace with spatial index if needed)
                        for (int kk = 0; kk < K; kk++) {
                            knnDist[kk] = Double.POSITIVE_INFINITY;
                            knnIdx[kk] = -1;
                        }
                        for (int s = 0; s < nSpurs; s++) {
                            double dx = spurX[s] - x;
                            double dy = spurY[s] - y;
                            double d = Math.hypot(dx, dy);

                            // insert into sorted knn arrays if smaller than largest
                            if (d < knnDist[K - 1]) {
                                // insertion sort shift
                                int pos = K - 1;
                                while (pos > 0 && d < knnDist[pos - 1]) {
                                    knnDist[pos] = knnDist[pos - 1];
                                    knnIdx[pos] = knnIdx[pos - 1];
                                    pos--;
                                }
                                knnDist[pos] = d;
                                knnIdx[pos] = s; // store spur array index
                            }
                        }

                        // if nearest spur distance is infinite (no spurs), skip
                        if (knnIdx[0] < 0) continue;

                        // exact spur location -> copy its value
                        if (knnDist[0] <= EPS) {
                            int spurArrayIndex = knnIdx[0];
                            int spurGlobalIndex = spurIdx[spurArrayIndex];
                            heightmap[i] = heightmap[spurGlobalIndex];
                            continue;
                        }

                        // compute weighted average of contributions from K nearest spurs
                        double weightSum = 0.0;
                        double weightedValue = 0.0;
                        for (int kk = 0; kk < K; kk++) {
                            int sIndex = knnIdx[kk];
                            if (sIndex < 0) break;
                            double d = knnDist[kk];

                            // inverse-distance weight
                            double wgt = 1.0 / Math.pow(d + EPS, IDW_POWER);

                            // per-spur factor mixing spur distance and outline distance
                            double denom = d + distOutline;
                            double factor = (denom == 0.0) ? 0.0 : 1.0 - (d / denom);
                            factor = Functions.applyFunction(hmFunc, factor);

                            int spurGlobalIndex = spurIdx[sIndex];
                            int spurValue = heightmap[spurGlobalIndex];

                            weightedValue += wgt * (factor * spurValue);
                            weightSum += wgt;
                        }

                        if (weightSum == 0.0) {
                            // fallback: leave zero
                            continue;
                        }

                        int value = (int) Math.round(weightedValue / weightSum);
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

    /*
    in stead of giving a heigthmap of integers, 
    this creates an array where each position represents the location in the heightmap
    each point stores 2 values, a factor that determines the height based on the closest spur
    and the max height, this will be used to determine the heigth of the pixels.
    */
    public static float[][] createFactorHeightArray(
        List<Integer> outline,
        List<Integer> spurs,
        int height,
        int width,
        int spurFunction
    ) throws InterruptedException, ExecutionException {
        final int size = width * height;

        float[][] factorHeight = new float[size][2];
        if (spurs == null || spurs.isEmpty()) {
            return factorHeight;
        }

        final int center = Helper.findCenter(outline, width);
        System.out.println("maxheigthwidth" + height + ", " + width);
        System.out.println("location center: x,y -> " + center % width + ", " + center/width);

        // First, compute spur values (single-threaded call you already had)
        final int[] heightmap = applySpurs(spurs, outline, height, width, center, spurFunction);

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
                        if (spurMask[i]) {
                            factorHeight[i][0] = 1.0f;           // A spur is at 100% of its own height
                            factorHeight[i][1] = (float) heightmap[i]; 
                            continue; 
                        }
                        int closestSpur = Helper.findClosest(i, spurs, w);
                        if (closestSpur < 0 || closestSpur >= size) {
                            // no valid spur: leave as is (0) or handle differently
                            continue;
                        }
                        factorHeight[i][1] = (float) heightmap[closestSpur];
                        

                        int sx = closestSpur % w;
                        int sy = closestSpur / w;

                        double distSpur = Helper.distance(sx, sy, x, y);
                        double distOutline = Helper.findClosestDistance(i, outline, w);
                        double denom = distSpur + distOutline;
                        double factor = 1.0 - (distSpur / denom); // = distOutline/denom
                        factorHeight[i][0] = (float) factor;
                    }
                }
            }));
        }

        // wait for tasks
        for (Future<?> f : futures) f.get();
        pool.shutdown();
        return factorHeight;
    }

    // converts the returnvalue of createFactorHeightArray and turns it into a heightmap
    public static int[] makeHeightmap(float[][] factorHeight, int function) {
        int[] heightmap = new int[factorHeight.length];
        
        for (int i = 0; i < heightmap.length; i++) {
            double factor = Functions.applyFunction(function, (double) factorHeight[i][0]);
            int value = (int) Math.round(factor * factorHeight[i][1]);
            heightmap[i] = Helper.clamp255(value);
        }
        return heightmap;
    }
}