import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Precompute {
    private static final int[][] NEIGHBORS = {
        {1, 3},         // a (0) 
        {0, 2, 3, 4},   // b (1) 
        {1, 4},         // c (2) 
        {0, 1, 5, 6},   // d (3) 
        {1, 2, 6, 7},   // e (4) 
        {3, 6},         // f (5) 
        {3, 4, 5, 7},   // g (6) 
        {4, 6}          // h (7) 
        };

    private static int countIslands(int mask) {

        Set<Integer> visited = new HashSet<>();
        int islands = 0;
        for (int i = 0; i < 8; i++) {
            if (isBitSet(mask, i) && !visited.contains(i)) {
                islands++; 
                dfs(mask, i, visited);
            }
        }
    return islands;
    }
    
    private static void dfs(int mask, int pos, Set<Integer> visited) {

        visited.add(pos);
        for (int neighbor : NEIGHBORS[pos]) {
            if (isBitSet(mask, neighbor) && !visited.contains(neighbor)) {
                dfs(mask, neighbor, visited);
            }
        }
    }

    private static boolean isBitSet(int mask, int bit) {
        return ((mask >> bit) & 1) == 1;
    }

    public static int[] precomputeBlacklist() {

        List<Integer> blacklist = new ArrayList<>();
        for (int i = 0; i < 256; i++) {
            if (countIslands(i) > 1) {
                blacklist.add(i);
            }
        }
        System.out.println("Made the blacklist.");
        return blacklist.stream()
        .mapToInt(Integer::intValue)
        .toArray();
    }

    public static void main(String[] args) {
        
        int mask = 0b11000011;
        System.out.println(countIslands(mask));

        int mask2 = 0b10000011;                     // a, b, h 
        System.out.println(countIslands(mask2));    // 2
        
        int mask3 = 0b10100101;                     // d, f, g connected diagonally 
        System.out.println(countIslands(mask3));    // 4
    }
}
