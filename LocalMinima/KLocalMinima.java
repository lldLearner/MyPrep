Code Review FollowUp 3
✅ 3. Return K Local Minima (Follow-up)
Part A — Easiest Correct Approach (O(n))
Since all minima must be checked anyway:
List<Integer> mins = allLocalMinima(arr);
return first k elements;


Part B — Priority-Based Approach (Better if K << n)
If we only want k smallest local minima by value, not by index:
Scan array once: O(n)


Each time you find a local minima → push to max-heap (size k)


If heap size > k → pop largest


Time:
O(n log k)

This is optimal for "return k smallest minima".

Part C — Streaming Output Approach
If array is huge:
Process in chunks


Keep a “sliding” value from previous chunk


Maintain global heap of size k


This allows working on arbitrarily large data.

✔ Full Java Snippet (O(n log k))
List<Integer> kLocalMinima(int[] arr, int k) {
    PriorityQueue<Integer> maxHeap = new PriorityQueue<>((a, b) -> b - a);
    int n = arr.length;
    
    for (int i = 0; i < n; i++) {
        boolean leftOk = (i == 0) || arr[i - 1] >= arr[i];
        boolean rightOk = (i == n - 1) || arr[i] <= arr[i + 1];
        
        if (leftOk && rightOk) {
            maxHeap.add(arr[i]);
            if (maxHeap.size() > k)
                maxHeap.poll(); // pop largest
        }
    }
    return new ArrayList<>(maxHeap);
}
