package ParkingSpot; 
import java.util.LinkedList; 
import java.util.Queue; 
public class Terrain { 
 private int row, col; 
 private int[][] terrain; 
 public Terrain(int row, int col) { 
  super(); 
  this.row = row; 
  this.col = col; 
  this.terrain = new int[row][col]; 
 } 
 public boolean isLand(int x, int y) { 
  return terrain[x][y] == 1; 
 } 
 public void addLand(int x, int y) { 
  terrain[x][y] = 1; 
 } 
 public int getIslands() { 
  int ct = 0; 
  boolean[][] vis = new boolean[row][col]; 
  for (int r = 0; r < row; r++) { 
   for (int c = 0; c < col; c++) { 
    if (terrain[r][c] == 1 && !vis[r][c]) { 
     countIslands(r, c, vis); 
     ct++; 
    } 
   } 
  } 
  return ct; 
 } 
 public void countIslands(int r, int c, boolean[][] vis) { 
  int[] dx = { 0, 0, 1, -1 }; 
  int[] dy = { 1, -1, 0, 0 }; 
  Queue<int[]> bfs = new LinkedList<>(); 
  bfs.add(new int[] { r, c }); 
  vis[r][c] = true; 
  while (!bfs.isEmpty()) { 
   int[] front = bfs.remove(); 
   int x = front[0]; 
   int y = front[1]; 
   for (int i = 0; i < 4; i++) { 
    int newX = x + dx[i]; 
    int newY = y + dy[i]; 
    if (newX >= 0 && newY >= 0 && newX < row && newY < 
col && terrain[newX][newY] == 1) { 
     if (!vis[newX][newY]) { 
      bfs.add(new int[] { newX, newY }); 
      vis[newX][newY] = true; 
     } 
    } 
   } 
  } 
 } 
} 
“We scan the full matrix once, and BFS explores each land cell at most once because of the visited array. So total time 
complexity is O(R*C). Even though BFS is called multiple times, each cell is visited only once across all BFS calls.” 
