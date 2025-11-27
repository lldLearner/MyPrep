package ParkingSpot;

import java.util.HashSet;
import java.util.Set;

public class TerrainConfigurable {

    private final DSU dsu = new DSU();
    private final Set<DSU.Point> land = new HashSet<>();
    private int islandCount = 0;

    private final int[][] dirs;

    public TerrainConfigurable(int[][] directions) {
        this.dirs = directions;
    }

    public void addLand(int x, int y) {
        DSU.Point p = new DSU.Point(x, y);

        if (land.contains(p)) return;

        land.add(p);
        dsu.makeSet(p);
        islandCount++;

        for (int[] d : dirs) {
            DSU.Point neigh = new DSU.Point(x + d[0], y + d[1]);

            if (land.contains(neigh)) {
                if (dsu.union(p, neigh)) {
                    islandCount--;
                }
            }
        }
    }

    public int getIslands() {
        return islandCount;
    }
}


package ParkingSpot;

public class Main8Neighbour {
    public static void main(String[] args) {

        int[][] DIR_8 = {
                {1,0},{-1,0},{0,1},{0,-1},
                {1,1},{1,-1},{-1,1},{-1,-1}
        };

        TerrainConfigurable t = new TerrainConfigurable(DIR_8);

        t.addLand(0,0);
        t.addLand(1,1);
        System.out.println(t.getIslands()); // 1 (diagonal merge)
    }
}

