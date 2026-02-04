import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;

public class Object {

    /**
     * @param data         1D array of height values (0–255)
     * @param width        width of the heightmap
     * @param height       height of the heightmap
     * @param maxWidth     max size of the object in X direction
     * @param maxDepth     max size of the object in Z direction
     * @param heightFactor 0.0–1.0 typically, controls max height
     * @param outputPath   path to output .obj file
     */
    public static void writeOBJ(
            int[] data,
            int width,
            int height,
            float maxWidth,
            float maxDepth,
            float heightFactor,
            String outputPath
    ) throws IOException {

        if (data.length != width * height) {
            throw new IllegalArgumentException("Data size does not match width * height");
        }

        float xScale = maxWidth / (width - 1f);
        float yScale = maxDepth / (height - 1f); // Using Y for the horizontal depth
        float maxVertical = Math.min(maxWidth, maxDepth) * heightFactor;

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {
            writer.write("# Blender Compatible Z-Up Terrain\n");

            // ===== Write vertices =====
            for (int row = 0; row < height; row++) {
                for (int col = 0; col < width; col++) {
                    int index = row * width + col;
                    float normalizedHeight = data[index] / 255.0f;

                    // Blender Coordinates:
                    // X = width, Y = depth (horizontal), Z = height (vertical)
                    float vx = col * xScale - maxWidth / 2f;
                    float vy = row * yScale - maxDepth / 2f;
                    float vz = normalizedHeight * maxVertical; 

                    writer.write(String.format(Locale.US, "v %.6f %.6f %.6f%n", vx, vy, vz));
                }
            }

            // ===== Write faces =====
            for (int row = 0; row < height - 1; row++) {
                for (int col = 0; col < width - 1; col++) {
                    
                    // OBJ uses 1-based indexing
                    // Row-major indexing: (row * width) + column + 1
                    int v1 = (row * width) + col + 1;           // Bottom-left
                    int v2 = (row * width) + (col + 1) + 1;     // Bottom-right
                    int v3 = ((row + 1) * width) + col + 1;     // Top-left
                    int v4 = ((row + 1) * width) + (col + 1) + 1; // Top-right

                    // Two triangles per quad (Counter-Clockwise winding)
                    // Triangle 1
                    writer.write(String.format("f %d %d %d%n", v1, v2, v4));
                    // Triangle 2
                    writer.write(String.format("f %d %d %d%n", v1, v4, v3));
                }
            }
        }
    }

    // ===== Example usage =====
    public static void main(String[] args) throws IOException {

        int width = 128;
        int height = 128;
        int[] heightmap = new int[width * height];

        // Example: simple gradient
        for (int z = 0; z < height; z++) {
            for (int x = 0; x < width; x++) {
                int index = z * width + x;
                // Create a pyramid/spike in the center
                int distToCenter = Math.abs(x - 64) + Math.abs(z - 64);
                heightmap[index] = Math.max(0, 255 - (distToCenter * 4));
            }
        }

        writeOBJ(
                heightmap,
                width,
                height,
                10.0f,    // max width
                10.0f,    // max depth
                0.3f,     // height factor
                "terrain.obj"
        );
    }
}
