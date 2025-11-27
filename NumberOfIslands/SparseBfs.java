Sparse 
import java.util.*; 
 
public class Terrain { 
 
    static class Point { 
        int x, y; 
 
        Point(int x, int y) { 
            this.x = x; 
            this.y = y; 
        } 
 
        @Override 
        public boolean equals(Object obj) { 
            if (this == obj) return true; 
            if (!(obj instanceof Point)) return false; 
            Point p = (Point) obj; 
            return x == p.x && y == p.y; 
        } 
 
        @Override 
        public int hashCode() { 
            return Objects.hash(x, y); 
        } 
    } 
 
    private int rows, cols; 
    private Set<Point> land = new HashSet<>(); 
 
    public Terrain(int rows, int cols) { 
        this.rows = rows; 
        this.cols = cols; 
    } 
 
    public boolean isLand(int x, int y) { 
        return land.contains(new Point(x, y)); 
    } 
 
    public void addLand(int x, int y) { 
        land.add(new Point(x, y)); 
    } 
 
    public int getIslandsSparse() { 
        Set<Point> visited = new HashSet<>(); 
        int count = 0; 
 
        for (Point p : land) { 
            if (!visited.contains(p)) { 
                bfs(p, visited); 
                count++; 
            } 
        } 
        return count; 
    } 
 
    private void bfs(Point start, Set<Point> visited) { 
        int[] dx = {1, -1, 0, 0}; 
        int[] dy = {0, 0, 1, -1}; 
 
        Queue<Point> q = new LinkedList<>(); 
        visited.add(start); 
        q.add(start); 
 
        while (!q.isEmpty()) { 
            Point cur = q.poll(); 
            for (int i = 0; i < 4; i++) { 
                int nx = cur.x + dx[i]; 
                int ny = cur.y + dy[i]; 
 
                Point next = new Point(nx, ny); 
                if (nx >= 0 && ny >= 0 && nx < rows && ny < cols && 
                        land.contains(next) && !visited.contains(next)) { 
                    visited.add(next); 
                    q.add(next); 
                } 
            } 
        } 
    } 
} 
 
Let L = number of land points. 
✔ addLand 
O(1) 
 
✔ isLand 
O(1) 
 
✔ getIslandsSparse 
Outer loop: 
O(L) 
 
BFS total work: 
O(L) 
 
Total Time: 
O(L) 
 
Space: 
O(L) for land set 
O(L) for visited 
O(L) for queue 
= O(L) 
