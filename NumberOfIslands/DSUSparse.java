DSU 
package ParkingSpot; 
 
import java.util.HashMap; 
import java.util.HashSet; 
import java.util.Map; 
import java.util.Objects; 
import java.util.Set; 
 
class Terrain { 
 
    private Map<Point, Point> parent = new HashMap<>(); 
    private Map<Point, Integer> rank = new HashMap<>(); 
    private Set<Point> land = new HashSet<>(); 
    private int islandCount = 0; 
 
    static class Point { 
        int x, y; 
        Point(int x, int y) { this.x = x; this.y = y; } 
 
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
    } 
 
    public void addLand(int x, int y) { 
        Point p = new Point(x, y); 
        if (land.contains(p)) return; 
 
        land.add(p); 
        parent.put(p, p); 
        rank.put(p, 0); 
 
        islandCount++; 
 
        int[][] dirs = {{1,0},{-1,0},{0,1},{0,-1}}; 
        for (int[] d : dirs) { 
            Point neigh = new Point(x + d[0], y + d[1]); 
            if (land.contains(neigh)) { 
                if (union(p, neigh)) { 
                    islandCount--; 
                } 
            } 
        } 
    } 
 
    public int getIslands() { 
        return islandCount; 
    } 
 
    private Point find(Point p) { 
        if (!parent.get(p).equals(p)) { 
            parent.put(p, find(parent.get(p))); 
        } 
        return parent.get(p); 
    } 
 
    private boolean union(Point a, Point b) { 
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
 
Matrix + DFS O(1) O(R*C) O(R*C) dense small grid 
 
Set (Sparse) + DFS O(1) O(L) O(L) sparse grid 
 
DSU (Array) O(1) O(1) O(R*C) dynamic updates, fixed grid 
 
DSU (Sparse HashMap) O(1) O(1) O(L) infinite grid, negatives, dynamic updates 
