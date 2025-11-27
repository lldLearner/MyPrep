4️⃣ Prefix Sum (Difference Array) — O(N) → The Optimal Solution

This is the best answer interviewers expect.

Insight:

Instead of incrementing every K in the range:

diff[L+1]++
diff[H+2]--


Then prefix sum gives support count for each K.

Dry-run for example:

Build diff array of size N+3 (9):

People:
[1,2] → L+1=2, H+2=4  → diff[2]++, diff[4]--
[1,4] → 2, 6          → diff[2]++, diff[6]--
[0,3] → 1, 5          → diff[1]++, diff[5]--
[0,1] → 1, 3          → diff[1]++, diff[3]--
[3,4] → 4, 6          → diff[4]++, diff[6]--
[0,2] → 1, 4          → diff[1]++, diff[4]--


Final diff array (index 1 to 6):

k:     1 2 3 4 5 6
diff:  3 2 -1 1 -1 - ?


Prefix sum gives active supporters at each K:

K=1: 3
K=2: 5
K=3: 4
K=4: 3
K=5: 2
K=6: ---


Check K ≥ supporters:

K=1 → 3≥1 ✔
K=2 → 5≥2 ✔
K=3 → 4≥3 ✔
K=4 → 3≥4 ✖
K=5 → 2≥5 ✖

static int solutionFour(int n, int[][] constraints) { 
  int[] diff = new int[n + 3]; 
  for (int i = 0; i < n; i++) { 
   int l = constraints[i][0]; 
   int r = constraints[i][1]; 
    diff[l + 1] += 1; 
    diff[r + 2] -= 1; 
  } 
  int active = 0; 
  int idx = 0; 
  int maxK = 0; 
  for (int k = 1; k <= n; k++) { 
   active += diff[k]; 
   if (active >= k) { 
    maxK = k; 
   } 
  } 
  return maxK; 
 } 
 public static void main(String[] args) { 
  // TODO Auto-generated method stub 
  int[][] constraints = { { 1, 2 }, { 1, 4 }, { 0, 3 }, { 0, 1 }, 
{ 3, 4 }, { 0, 2 } }; 
  System.out.println(solutionFour(constraints.length, 
constraints)); // Output: 3 
 } 
} 
Answer = 3
