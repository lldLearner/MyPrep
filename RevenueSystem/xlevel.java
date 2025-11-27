 double getRevenueUptoXLevel(int id, int x) { 
  Queue<int[]> bfs = new LinkedList<>(); 
  bfs.add(new int[] { id, 1 }); 
  int revenue = 0; 
  Set<Integer> vis = new HashSet<>(); 
  vis.add(id); 
  while (!bfs.isEmpty()) { 
 
   int[] front = bfs.poll(); 
   int idf = front[0]; 
   int level = front[1]; 
   revenue += revenueMap.get(idf); 
   // need to process all children at the level , keep in mind 
   if (level == x) { 
    continue; 
   } 
   for (int neighbour : graph.getOrDefault(idf, new ArrayList<>())) { 
    if (!vis.contains(neighbour)) { 
     vis.add(neighbour); 
     bfs.add(new int[] { neighbour, level + 1 }); 
    } 
   } 
  } 
 
  return revenue; 
 } 
} 
 
where V = number of visited customers within X levels, 
 and E = total referral edges explored within X levels. 
Q1: Big-O Complexities for HashMap & TreeMap (Insert / Delete / Get) 
 
Operation HashMap 
(Average) 
HashMap (Worst-case) TreeMap (Red-Black 
Tree) 
Insert (put) O(1) O(log n) (after Java 8 
tree-ification) 
O(log n) 
Get (get) O(1) O(log n) O(log n) 
Delete 
(remove) 
O(1) O(log n) O(log n) 
Traverse O(n) O(n) O(n) (in sorted order) 
Before Java 8, worst-case was O(n) due to long linked-list chains on collisions. 
But after Java 8: 
● When a bucket exceeds threshold (default = 8) 
● Linked list is converted into a Red-Black Tree 
● So lookup becomes O(log n) (tree node search) 
● Worst case is O(log n) instead of O(n) 
Q2: Deeper Dive into HashMap Implementation (Java 8+) 
A HashMap is: 
Array<Node<K,V>> table;  // buckets 
Each bucket contains linked list or red-black tree: 
Bucket → [Node] → [Node] → ...   (if few collisions) 
Bucket → RB Tree of Nodes         
(if many collisions) 
Put(key, value) 
1. Compute hash using hash(key) — spreads bits to reduce 
collisions. 
2. Determine bucket index: (n - 1) & hash 
3. If bucket empty → create node and store. 
4. If bucket has nodes: 
○ Compare keys using equals 
○ If same key → update value 
○ Else insert in bucket: 
■ If chain length < 8 → append to linked list 
■ If chain length ≥ 8 → treeify → convert to 
Red-Black Tree 
5. Resize if size exceeds threshold loadFactor * capacity 
(default LF=0.75) 
get(key): 
1. Compute hash → go to bucket 
2. If bucket is: 
○ Linked list → linear search inside bucket 
○ Red-Black Tree → tree search O(log n) 
resize(): 
● When size exceeds load factor threshold 
● Capacity doubles 
● All nodes rehashed 
● Costly operation but infrequent 
Q3: RBT 
TreeMap is a Red-Black Tree implementation (self-balancing BST). 
Characteristics: 
● Keeps keys sorted 
● In-order traversal gives natural order 
● Custom ordering supported via Comparator 
Insert workflow: 
1. Compare new key with existing keys via compareTo 
2. Find correct leaf node using BST logic 
3. Insert node 
4. Rebalance RB tree (color flips, rotations) 
 
Get: 
Binary search on RBTree → O(log n) 
Remove: 
1. Standard BST delete logic 
 
2. Rebalance RB tree (rotations, recoloring) 
 
3. Always stays balanced → height approx log2(n) 
