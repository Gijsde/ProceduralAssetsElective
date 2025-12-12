package src;

public class Noise {
    
    public static void randomNoise(int[][] heightMap, int multipier) {

        for (int[] heightMap1 : heightMap) {
            for (int x = 0; x < heightMap[0].length; x++) {
                if (heightMap1[x] < 255) {
                    double random = multipier * (Math.random() * 2 - 1);
                    int value = heightMap1[x] + (int) random;
                    heightMap1[x] = Math.max(0, Math.min(255, value));
                }
            }
        }
    }
}
