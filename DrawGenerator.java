Q)DrawGenerator 
import java.util.*; 
public class TennisSchedulingProblemPart1 { 
 // ------------------------------------------ 
 // 1. Simulate tournament rounds (corrected) 
 // ------------------------------------------ 
 static List<List<Integer>> printDraw(List<Integer> ranks) { 
  List<List<Integer>> ans = new ArrayList<>(); 
  List<Integer> curr = new ArrayList<>(ranks); 
  while (curr.size() > 1) { 
   ans.add(new ArrayList<>(curr)); 
   List<Integer> next = new ArrayList<>(); 
   for (int i = 0; i < curr.size(); i += 2) { 
    next.add(Math.min(curr.get(i), curr.get(i + 1))); // FIXED: 
use curr, not ranks 
   } 
   curr = next; 
  } 
  ans.add(curr); // champion round 
  return ans; 
 } 
 // ------------------------------------------------ 
 // 2. Generate feasible draw until size == n (power of two) 
 // ------------------------------------------------ 
 static List<Integer> generateFeasibleDraw(List<Integer> curr, int n) { 
  if (curr.size() == n) 
   return curr; 
  List<Integer> next = new ArrayList<>(); 
  for (int player : curr) { 
   next.add(player); 
   next.add(2 * curr.size() - player + 1); 
  } 
  return generateFeasibleDraw(next, n); 
 } 
 // --------------------------------------- 
 // 3. Helpers: power of two 
 // --------------------------------------- 
 static boolean isPowerOfTwo(int n) { 
  return (n & (n - 1)) == 0; 
 } 
 static int nextPowerOfTwo(int n) { 
  int p = 1; 
 
  while (p < n) 
   p <<= 1; 
  return p; 
 } 
 // ------------------------------------------------ 
 // 4. General draw generation (handles non-powers) 
 // ------------------------------------------------ 
 static List<Integer> generateDrawGeneral(List<Integer> curr, int n) { 
  int neededSize = isPowerOfTwo(n) ? n : nextPowerOfTwo(n); 
  List<Integer> draw = generateFeasibleDraw(curr, neededSize); 
  // Replace invalid players (generated artificially) 
  for (int i = 0; i < draw.size(); i++) { 
   if (draw.get(i) > n) { 
    draw.set(i, -1); 
   } 
  } 
  return draw; 
 } 
 // ------------------------------------------------------------ 
 // 5. Generate ALL possible bracket permutations (exponential) 
 // ------------------------------------------------------------ 
 static List<List<Integer>> generateAllPossibleDraws(List<Integer> draw) { 
  int n = draw.size(); 
  if (n == 1) { 
   List<List<Integer>> base = new ArrayList<>(); 
   base.add(new ArrayList<>(draw)); 
   return base; 
  } 
  int mid = n / 2; 
  // Copy sublists (avoid buggy subList) 
  List<Integer> leftSide = new ArrayList<>(draw.subList(0, mid)); 
  List<Integer> rightSide = new ArrayList<>(draw.subList(mid, n)); 
  List<List<Integer>> leftDraws = generateAllPossibleDraws(leftSide); 
  List<List<Integer>> rightDraws = generateAllPossibleDraws(rightSide); 
  List<List<Integer>> ans = new ArrayList<>(); 
  for (List<Integer> L : leftDraws) { 
   for (List<Integer> R : rightDraws) { 
    // L followed by R 
    List<Integer> a = new ArrayList<>(); 
    a.addAll(L); 
    a.addAll(R); 
    // R followed by L 
    List<Integer> b = new ArrayList<>(); 
    b.addAll(R); 
    b.addAll(L); 
    ans.add(a); 
    ans.add(b); 
   } 
  } 
  return ans; 
 } 
 // ------------------------------------------------------------ 
 // TESTING 
 // ------------------------------------------------------------ 
 public static void main(String[] args) { 
  List<Integer> seeds = Arrays.asList(1, 2, 3, 4); 
  System.out.println("Feasible Draw:"); 
  System.out.println(generateDrawGeneral(seeds, 4)); 
  System.out.println("\nTournament Rounds:"); 
  System.out.println(printDraw(seeds)); 
  System.out.println("\nAll Possible Draws:"); 
  List<List<Integer>> all = generateAllPossibleDraws(seeds); 
  for (List<Integer> d : all) { 
   System.out.println(d); 
  } 
 } 
} 
