
import java.awt.geom.Point2D;

public class Noise {

    // Return a pseudo-random unit gradient vector for integer grid coordinate (ix, iy)
    private static Point2D.Float randomGradient(int ix, int iy) {
        // Simple integer hash mixing with 32-bit wrap-around
        long a = ix & 0xFFFFFFFFL;
        long b = iy & 0xFFFFFFFFL;

        a = (a * 3284157443L) & 0xFFFFFFFFL;
        b ^= ((a << 16) | (a >>> 16)) & 0xFFFFFFFFL;
        b = (b * 1911520717L) & 0xFFFFFFFFL;
        a ^= ((b << 16) | (b >>> 16)) & 0xFFFFFFFFL;
        a = (a * 2048419325L) & 0xFFFFFFFFL;

        // Map 32-bit value to angle [0, 2*PI)
        float angle = (float) ((a & 0xFFFFFFFFL) / (double)0x100000000L * (2.0 * Math.PI));
        float gx = (float) Math.cos(angle);
        float gy = (float) Math.sin(angle);
        return new Point2D.Float(gx, gy);
    }

    private static float dotGridGradient(int ix, int iy, float x, float y) {
        // Gets gradient from integer coordinates
        Point2D.Float gradient = randomGradient(ix, iy);

        // Compute the distance vector
        float dx = x - (float) ix;
        float dy = y - (float) iy;

        // Compute the dot-product
        return dx * gradient.x + dy * gradient.y;
    }

    // Perlin fade function (smoother): 6t^5 - 15t^4 + 10t^3
    private static float fade(float t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }

    // Linear interpolation using fade
    private static float interpolate(float a0, float a1, float w) {
        float f = fade(w);
        return a0 + f * (a1 - a0);
    }

    private static float perlin(float x, float y) {
        // Determine grid cell corner coordinates (floor)
        int x0 = (int) Math.floor(x);
        int y0 = (int) Math.floor(y);
        int x1 = x0 + 1;
        int y1 = y0 + 1;

        // Compute interpolation weights
        float sx = x - (float) x0;
        float sy = y - (float) y0;

        // Compute and interpolate top two corners
        float n0 = dotGridGradient(x0, y0, x, y);
        float n1 = dotGridGradient(x1, y0, x, y);
        float ix0 = interpolate(n0, n1, sx);

        n0 = dotGridGradient(x0, y1, x, y);
        n1 = dotGridGradient(x1, y1, x, y);
        float ix1 = interpolate(n0, n1, sx);

        return interpolate(ix0, ix1, sy);
    }

    /**
     * Generate Perlin noise into a 1D heightMap array (row-major).
     *
     * @param heightMap 1D array of length width * height (row-major: index = y*width + x)
     * @param randomness controls base grid size (must be >= 1)
     * @param width image width
     * @param height image height
     */
    public static int[] generatePerlin(int randomness, int height, int width) {
        
        int[] heightMap = new int[height * width];

        if (randomness <= 0) randomness = 1;

        // Guard against GRID_SIZE == 0
        int GRID_SIZE = Math.max(1, height / Math.max(1, randomness));

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {

                float val = 0f;
                float freq = 1f;
                float amp = 1f;

                // multiple octaves for fractal noise
                for (int i = 0; i < 12; i++) {
                    val += perlin(x * freq / GRID_SIZE, y * freq / GRID_SIZE) * amp;

                    freq *= 2f;
                    amp /= 2f;
                }
                // Add contrast
                val *= 1.2f;

                if (val > 1.0f) val = 1.0f;
                else if (val < -1.0f) val = -1.0f;

                int color = (int) (((val + 1.0f) * 0.5f) * 255);
                // clamp just in case
                if (color < 0) color = 0;
                else if (color > 255) color = 255;

                int idx = y * width + x;
                heightMap[idx] = color;
            }
        }
        System.out.println("Generated Noise");
        return heightMap;
    }

    public static int[] applyNoise(int[] heightmap, int[] noise, double strength) {
        int[] endMask = new int[heightmap.length];

        for (int i = 0; i < endMask.length; i++) {
            endMask[i] = Helper.clamp255((int) ((heightmap[i] + (noise[i] * strength)) / (strength + 1)));
        }
        System.out.println("Applied noise.");
        return endMask;
    }
}