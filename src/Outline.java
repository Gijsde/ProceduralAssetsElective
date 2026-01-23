import java.util.List;

public class Outline {

    private static boolean getSafe1D(boolean[] mask, int x, int y, int width, int height) {
        // Check if y is within [0, height-1] and x is within [0, width-1]
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return false; 
        }
        return mask[y * width + x];
    }

    private static int encodeDonut(boolean[] mask, int location, int height, int width) {
        int grid = 0b0;
        int x = location%width;
        int y = location/width;

        if (getSafe1D(mask, x - 1, y - 1, width, height)) grid |= 1;
        if (getSafe1D(mask, x    , y - 1, width, height)) grid |= 1 << 1;
        if (getSafe1D(mask, x + 1, y - 1, width, height)) grid |= 1 << 2;
        if (getSafe1D(mask, x - 1, y    , width, height)) grid |= 1 << 3;
        if (getSafe1D(mask, x + 1, y    , width, height)) grid |= 1 << 4;
        if (getSafe1D(mask, x - 1, y + 1, width, height)) grid |= 1 << 5;
        if (getSafe1D(mask, x    , y + 1, width, height)) grid |= 1 << 6;
        if (getSafe1D(mask, x + 1, y + 1, width, height)) grid |= 1 << 7;

        return grid;
    }

    private static boolean isRemovable(int value, int[] blacklist) {
        for (int i : blacklist) {
            if (i == value) return false;
        }
        return true;
    }

    public static List<Integer> cleanupMask(boolean[] mask, int height, int width, int[] blacklist) {
        // if (mask == null || blacklist == null) return mask;

        boolean hasRemoved = true;

        while (hasRemoved) {
            hasRemoved = false;

            List<Integer> outline = Helper.findBlack(mask);

            for (int idx : outline) {
                if (!mask[idx]) continue;

                int encoded = encodeDonut(mask, idx, width, height);

                if (isRemovable(encoded, blacklist)) {
                    hasRemoved = true;
                    mask[idx] = false;
                }
            }
        }
        System.out.println("Cleaned up the image.");
        return Helper.findBlack(mask);
    }

}
