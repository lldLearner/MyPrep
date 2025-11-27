🎃 Haunted House Problem — Interview Explanation
Problem Statement (in your own words)

We have N people.
Each person i has a constraint:

They will go only if at least L[i] other people also go.

They refuse to go if more than H[i] other people go.

So if total group size = K:

Each person i is fine if:

L[i] ≤ (K - 1) ≤ H[i]


We want the maximum K possible such that K people are happy with K.

🧠 Correct Insight

For a given K, a person joins if:

L[i] + 1 ≤ K ≤ H[i] + 1


Because L and H are "other people", but K includes the person itself.

So each person supports all K in this interval.
This is like each person voting for a range of valid K.

We want:

#people supporting K   ≥ K

⭐ Your 4 Solutions Explained (Interview Style)
1️⃣ Brute Force — O(2^N) — Show interviewer you understand the search space

“A naive solution is to try all subsets of N people, check if each subset size K satisfies L ≤ K-1 ≤ H for all members.

That’s 2^N subsets → exponential → not feasible.”

2️⃣ O(N²) – Range counting with a frequency array
Idea:

For each person:

They support all values K in [L+1, H+1]


Maintain:

count[K] = how many people support group size K


Then:

Find max K such that count[K] ≥ K


Pretty direct.

Code snippet (your solutionTwo):
for each person:
    for (k = L+1; k <= H+1; k++) count[k]++

🧪 Dry-run for the Example

Input:

6 people
[1,2]
[1,4]
[0,3]
[0,1]
[3,4]
[0,2]


Let’s compute each person’s supported K range (K = number of total people):

Person	L	H	Supports K from →
P0	1	2	2 to 3
P1	1	4	2 to 5
P2	0	3	1 to 4
P3	0	1	1 to 2
P4	3	4	4 to 5
P5	0	2	1 to 3

Let’s fill count array:

K=1: P2, P3, P5 → 3
K=2: P0, P1, P2, P3, P5 → 5
K=3: P0, P1, P2, P5 → 4
K=4: P1, P2, P4 → 3
K=5: P1, P4 → 2


Now check:

K=1 → 3 ≥ 1 ✔
K=2 → 5 ≥ 2 ✔
K=3 → 4 ≥ 3 ✔
K=4 → 3 < 4 ✖
K=5 → 2 < 5 ✖


Maximum valid K = 3
  
Q)Haunted House 
package ParkingSpot; 
import java.util.ArrayList; 
import java.util.Arrays; 
import java.util.List; 
public class HauntedHouse { 
 static int solution(int n, int[][] constraints) { 
  for (int k = n; k >= 1; k--) { 
   // for each k check if k - 1 lies between l and h 
   int count = 0; 
   for (int i = 0; i < n; i++) { 
    int l = constraints[i][0]; 
    int r = constraints[i][1]; 
    if (l + 1 <= k && k <= r + 1) { 
     count++; 
    } 
    if (count == k) { 
     return k; 
    } 
   } 
  } 
  return 0; 
 }


static int solutionTwo(int n, int[][] constraints) { 
  int[] count = new int[n + 1]; 
  for (int i = 0; i < n; i++) { 
   int l = constraints[i][0]; 
   int r = constraints[i][1]; 
   for (int k = l + 1; k <= r + 1 && k <= n; k++) { 
    count[k]++; 
   } 
  } 
  System.out.print(Arrays.toString(count)); 
  int maxK = 0; 
  for (int k = 1; k <= n; k++) { 
   if (count[k] >= k) { 
    maxK = k; 
   } 
  } 
  return maxK; 
 } 
