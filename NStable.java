🏆 Interview Summary (What you say)

“This is a classic sliding window + min/max maintenance problem.

We want the largest window where max - min ≤ m.

We can solve it in O(n) using two monotonic deques:

one keeps max values (descending)

one keeps min values (ascending)

Each index enters and exits each deque at most once, giving O(n) total.

This gives the optimal solution.”

🧪 SMALL DRY RUN (Solution 5)
arr = [8,2,4,7]
m = 4

Step 1: right=0 → add 8

maxDeque = [8]
minDeque = [8]
Window = [8] → valid → size=1

Step 2: right=1 → add 2

maxDeque:

8 > 2 → keep
max = 8


minDeque:

2 < 8 → pop 8
minDeque = [2]
min = 2


Window = [8,2]
max - min = 8-2 = 6 > 4 → shrink

Remove left=0 (value=8)

maxDeque pops 8
Window becomes [2]


Valid, size=1.

Step 3: right=2 → add 4

maxDeque:

4 < 2? no
append → [2,4]
max=4


minDeque:

4 > 2 → keep
min=2


Window [2,4]
4-2=2 ≤4 valid → size=2.

Step 4: right=3 → add 7

maxDeque:

7 > 4 → pop 4
7 > 2 → pop 2
maxDeque = [7]
max=7


minDeque stays [2,4] but 2 is invalid:

7 - 2 = 5 >4 → shrink

left=1 (2 is at index 1, minDeque.pollFirst())

Now window = [4,7]
min=4 → valid → size=2.

🎉 Final Answer = 2
  
Q)NStable 
import java.util.ArrayDeque; 
import java.util.Deque; 
import java.util.TreeMap; 
public class NStable { 
 // O(n^4) solution 
 static int solutionOne(int n, int[] arr, int m) { 
  int maxSize = 0; 
  for (int i = 0; i < n; i++) { 
   for (int j = i; j < n; j++) { 
    boolean valid = true; 
    for (int x = i; x <= j && valid; x++) { 
     for (int y = i; y <= j; y++) { 
      if (Math.abs(arr[x] - arr[y]) > m) { 
       valid = false; 
       break; 
      } 
     } 
    } 
    if (valid) 
     maxSize = Math.max(maxSize, j - i + 1); 
   } 
  } 
  return maxSize; 
 } 
 // O(n^3) solution 
 static int solutionTwo(int n, int[] arr, int m) { 
  int maxSize = 0; 
  for (int i = 0; i < n; i++) { 
   for (int j = i; j < n; j++) { 
    int min = Integer.MAX_VALUE; 
    int max = Integer.MIN_VALUE; 
    for (int k = i; k <= j; k++) { 
     min = Math.min(arr[k], min); 
     max = Math.max(arr[k], max); 
    } 
    if (max - min <= m) { 
     maxSize = Math.max(maxSize, j - i + 1); 
    } 
   } 
  } 
  return maxSize; 
 } 
 // O(n^2) solution 
 static int solutionThree(int n, int[] arr, int m) { 
  int maxSize = 0; 
  for (int i = 0; i < n; i++) { 
   int min = Integer.MAX_VALUE; 
   int max = Integer.MIN_VALUE; 
   for (int j = i; j < n; j++) { 
    min = Math.min(min, arr[j]); 
    max = Math.max(max, arr[j]); 
    if (max - min <= m) { 
     maxSize = Math.max(maxSize, j - i + 1); 
    } 
   } 
  } 
  return maxSize; 
 } 
 // O(nlogn) solution 
 static int solutionFourth(int n, int[] arr, int m) { 
  int left = 0; 
  TreeMap<Integer, Integer> freq = new TreeMap<>(); 
  int maxSize = 0; 
  for (int right = 0; right < n; right++) { 
   freq.put(arr[right], freq.getOrDefault(arr[right], 0) + 
1); 
   while (freq.lastKey() - freq.firstKey() > m) { 
    int count = freq.get(arr[left]); 
    if (count == 1) { 
     freq.remove(arr[left]); 
    } else { 
     freq.put(arr[left], count - 1); 
    } 
    left++; 
   } 
   maxSize = Math.max(maxSize, right - left + 1); 
  } 
  return maxSize; 
 } 
 // O(n) solution 
 static int solutionFifth(int n, int[] arr, int m) { 
  Deque<Integer> maxDequeue = new ArrayDeque<>(); 
  Deque<Integer> minDequeue = new ArrayDeque<>(); 
  int maxSize = 0; 
  int left = 0; 
  for (int right = 0; right < n; right++) { 
   while (!maxDequeue.isEmpty() && maxDequeue.peekFirst() < 
arr[right]) { 
    maxDequeue.pollLast(); 
   } 
   maxDequeue.addLast(right); 
   while (!minDequeue.isEmpty() && minDequeue.peekFirst() > 
arr[right]) { 
    minDequeue.pollLast(); 
   } 
   minDequeue.addLast(right); 
   while (arr[maxDequeue.peekFirst()] - 
arr[minDequeue.peekFirst()] > m) { 
    if (maxDequeue.peekFirst() == left) 
     maxDequeue.pollFirst(); 
    if (minDequeue.peekFirst() == left) 
     minDequeue.pollFirst(); 
    left++; 
   } 
    
   maxSize = Math.max(maxSize, right - left + 1); 
  } 
   
  return maxSize; 
 } 
 pub
