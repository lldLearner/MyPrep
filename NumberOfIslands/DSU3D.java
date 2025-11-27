package ParkingSpot;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class DSU3D {

    public static class Point3D {
        public final int x, y, z;

        public Point3D(int x, int y, int z) {
            this.x = x; 
            this.y = y; 
            this.z = z;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Point3D)) return false;
            Point3D p = (Point3D) o;
            return x == p.x && y == p.y && z == p.z;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y, z);
        }

        @Override
        public String toString() {
            return "(" + x + "," + y + "," + z + ")";
        }
    }

    private final Map<Point3D, Point3D> parent = new HashMap<>();
    private final Map<Point3D, Integer> rank = new HashMap<>();

    public void makeSet(Point3D p) {
        parent.put(p, p);
        rank.put(p, 0);
    }

    public Point3D find(Point3D p) {
        if (!parent.get(p).equals(p)) {
            parent.put(p, find(parent.get(p)));
        }
        return parent.get(p);
    }

    public boolean union(Point3D a, Point3D b) {
        Point3D pa = find(a);
        Point3D pb = find(b);

        if (pa.equals(pb)) return false;

        int ra = rank.get(pa);
        int rb = rank.get(pb);

        if (ra < rb) parent.put(pa, pb);
        else if (rb < ra) parent.put(pb, pa);
        else {
            parent.put(pb, pa);
            rank.put(pa, ra + 1);
        }
        return true;
    }
}

package ParkingSpot;

import java.util.HashSet;
import java.util.Set;

public class Terrain3D {

    private final DSU3D dsu = new DSU3D();
    private final Set<DSU3D.Point3D> land = new HashSet<>();
    private int islandCount = 0;

    private final int[][] dirs;

    public Terrain3D(int[][] dirs) {
        this.dirs = dirs;
    }

    public void addLand(int x, int y, int z) {

        DSU3D.Point3D p = new DSU3D.Point3D(x, y, z);

        if (land.contains(p)) return;

        land.add(p);
        dsu.makeSet(p);
        islandCount++;

        for (int[] d : dirs) {
            DSU3D.Point3D neigh = new DSU3D.Point3D(x + d[0], y + d[1], z + d[2]);

            if (land.contains(neigh)) {
                if (dsu.union(p, neigh)) islandCount--;
            }
        }
    }

    public int getIslands() {
        return islandCount;
    }
}

package ParkingSpot;

public class Main3D {
    public static void main(String[] args) {

        int[][] DIR_6 = {
                {1,0,0},{-1,0,0},
                {0,1,0},{0,-1,0},
                {0,0,1},{0,0,-1}
        };

        Terrain3D t = new Terrain3D(DIR_6);

        t.addLand(0,0,0);
        t.addLand(1,0,0);
        t.addLand(0,1,0);
        System.out.println(t.getIslands()); // 1 (connected in 3D)
    }
}
