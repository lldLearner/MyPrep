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
