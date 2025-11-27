Code 
import java.util.ArrayList; 
import java.util.List; 
import java.util.PriorityQueue; 
public class LocalMinima { 
 // O(n) solution 
 static List<Integer> allLocalMinima(int n, int[] arr) { 
  List<Integer> ans = new ArrayList<>(); 
  for (int i = 0; i < n; i++) { 
   boolean isLeftOk = (i == 0) || arr[i - 1] >= arr[i]; 
   boolean isRightOk = (i == n - 1) || arr[i] <= arr[i + 1]; 
   if (isLeftOk && isRightOk) 
    ans.add(i); 
  } 
  return ans; 
 } 
 // O(logn) solution 
 static int aLocalMinima(int n, int[] arr) { 
  int l = 0, r = n - 1; 
  while (l <= r) { 
   int mid = (l + r) / 2; 
   boolean isLeftOk = (mid == 0) || arr[mid - 1] >= arr[mid]; 
   boolean isRightOk = (mid == n - 1) || arr[mid] <= arr[mid + 1]; 
   if (isLeftOk && isRightOk) { 
    return mid; 
   } 
   if (mid > 0 && arr[mid - 1] < arr[mid]) { 
    r = mid - 1; 
   } else { 
    l = mid + 1; 
   } 
  } 
  return -1; 
 } 
 // 2D local minima O(mnlogmn) 
 static class Point { 
  int val; 
  int r, c; 
  int dist; 
  public Point(int val, int r, int c, int dist) { 
   super(); 
   this.val = val; 
   this.r = r; 
   this.c = c; 
   this.dist = dist; 
  } 
 } 
 static Point twoDLocalMinima(int x, int y, int[][] arr) { 
  PriorityQueue<Point> pq = new PriorityQueue<>( 
    (a, b) -> a.dist == b.dist ? Integer.compare(a.val, b.val) : 
Integer.compare(a.dist, b.dist)); 
  pq.add(new Point(arr[x][y], x, y, 0)); 
  boolean[][] vis = new boolean[arr.length][arr[0].length]; 
  int[] dx = { 0, 0, -1, 1 }; 
  int[] dy = { 1, -1, 0, 0 }; 
  while (!pq.isEmpty()) { 
   Point front = pq.poll(); 
   int r = front.r; 
   int c = front.c; 
   if (vis[r][c]) 
    continue; 
   vis[r][c] = true; // MUST mark visited here 
   // Check if local minimum 
   boolean isLocal = true; 
   for (int i = 0; i < 4; i++) { 
    int nr = r + dx[i]; 
    int nc = c + dy[i]; 
 
    if (nr >= 0 && nc >= 0 && nr < arr.length && nc < arr[0].length) 
{ 
     if (arr[nr][nc] < front.val) { // CORRECT CONDITION 
      isLocal = false; 
      break; 
     } 
    } 
   } 
   if (isLocal) 
    return front; 
   // Add all neighbors (NOT only bigger ones!) 
   for (int i = 0; i < 4; i++) { 
    int nr = r + dx[i]; 
    int nc = c + dy[i]; 
    if (nr >= 0 && nc >= 0 && nr < arr.length && nc < arr[0].length 
&& !vis[nr][nc]) { 
     int newDist = Math.abs(nr - x) + Math.abs(nc - y); 
     pq.add(new Point(arr[nr][nc], nr, nc, newDist)); 
    } 
   } 
  } 
  return null; 
 } 
 public static void main(String[] args) { 
  System.out.println(allLocalMinima(4, new int[] { 4, 8, 2, 10 })); 
  System.out.println(allLocalMinima(7, new int[] { 7, 3, 5, 7, 9, 0, 2 })); 
  System.out.println(aLocalMinima(7, new int[] { 7, 3, 5, 7, 9, 0, 2 })); 
 } 
}
