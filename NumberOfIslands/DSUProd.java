package ParkingSpot;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class DSU {

    public static class Point {
        public final int x, y;

        public Point(int x, int y) {
            this.x = x; 
            this.y = y;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Point)) return false;
            Point p = (Point) o;
            return x == p.x && y == p.y;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }

        @Override
        public String toString() {
            return "(" + x + "," + y + ")";
        }
    }

    private final Map<Point, Point> parent = new HashMap<>();
    private final Map<Point, Integer> rank = new HashMap<>();

    public void makeSet(Point p) {
        parent.put(p, p);
        rank.put(p, 0);
    }

    public Point find(Point p) {
        if (!parent.get(p).equals(p)) {
            parent.put(p, find(parent.get(p))); // path compression
        }
        return parent.get(p);
    }

    public boolean union(Point a, Point b) {
        Point pa = find(a);
        Point pb = find(b);

        if (pa.equals(pb)) return false;

        int ra = rank.get(pa);
        int rb = rank.get(pb);

        if (ra < rb) {
            parent.put(pa, pb);
        } else if (rb < ra) {
            parent.put(pb, pa);
        } else {
            parent.put(pb, pa);
            rank.put(pa, ra + 1);
        }
        return true;
    }
}


package ParkingSpot;

import java.util.HashSet;
import java.util.Set;

public class Terrain {

    private final DSU dsu = new DSU();
    private final Set<DSU.Point> land = new HashSet<>();
    private int islandCount = 0;

    // Default 4-direction neighbourhood
    private final int[][] dirs = {{1,0},{-1,0},{0,1},{0,-1}};

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

public class Main {
    public static void main(String[] args) {

        Terrain t = new Terrain();

        t.addLand(0,0); 
        System.out.println(t.getIslands()); // 1

        t.addLand(0,1); 
        System.out.println(t.getIslands()); // 1 merged

        t.addLand(2,2);
        System.out.println(t.getIslands()); // 2

        t.addLand(2,1);
        System.out.println(t.getIslands()); // 2 merged
    }
}
