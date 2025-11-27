3️⃣ Sweep Line – O(N log N)

Instead of looping all K for each person, treat each person as adding a +1 to ALL valid K in their range using events:

+1 at (L+1)
-1 at (H+2)


Sort events → sweep from K=1 to N, maintaining active support count.

Your code:

events.add([L+1, +1])
events.add([H+2, -1])

static int solutionThree(int n, int[][] constraints) { 
  List<int[]> events = new ArrayList<>(); 
  for (int i = 0; i < n; i++) { 
   int l = constraints[i][0]; 
   int r = constraints[i][1]; 
   events.add(new int[] { l + 1, 1 }); 
   events.add(new int[] { r + 2, -1 }); 
  } 
  events.sort((a, b) -> Integer.compare(a[0], b[0])); 
  System.out.println(events); 
  int active = 0; 
  int idx = 0; 
  int maxK = 0; 
  for (int k = 1; k <= n; k++) { 
   while (idx < events.size() && events.get(idx)[0] == k) { 
    active += events.get(idx)[1]; 
    idx++; 
   } 
   if (active >= k) { 
    maxK = k; 
   } 
  } 
  return maxK; 
 } 
