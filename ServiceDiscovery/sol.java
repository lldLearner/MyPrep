package ParkingSpot;

import java.util.*;

public class ServiceDiscovery {

    private final Map<String, List<String>> deps;

    public ServiceDiscovery(Map<String, List<String>> deps) {
        this.deps = deps;
    }

    // Returns direct dependencies of a package
    List<String> getPackageBuildDependencies(String pkg) {
        return deps.getOrDefault(pkg, Collections.emptyList());
    }

    // ---------------------------------------------------------
    // MAIN FUNCTION: Return build order for given package
    // ---------------------------------------------------------
    List<String> dependencyResolver(String pkg) {

        // Step 1: Discover reachable components
        Set<String> reachable = new HashSet<>();
        discoverDFS(pkg, reachable);

        // Step 2: Build adjacency (dependency → dependent) and indegree
        Map<String, List<String>> adj = new HashMap<>();
        Map<String, Integer> indegree = new HashMap<>();

        for (String c : reachable) {
            adj.put(c, new ArrayList<>());
            indegree.put(c, 0);
        }

        for (String component : reachable) {
            for (String dep : getPackageBuildDependencies(component)) {
                adj.get(dep).add(component);
                indegree.put(component, indegree.get(component) + 1);
            }
        }

        // Step 3: Kahn's Topological Sort
        Queue<String> queue = new LinkedList<>();
        for (String c : indegree.keySet()) {
            if (indegree.get(c) == 0)
                queue.add(c);
        }

        List<String> buildOrder = new ArrayList<>();

        while (!queue.isEmpty()) {
            String node = queue.remove();
            buildOrder.add(node);

            for (String neighbor : adj.get(node)) {
                indegree.put(neighbor, indegree.get(neighbor) - 1);
                if (indegree.get(neighbor) == 0)
                    queue.add(neighbor);
            }
        }

        if (buildOrder.size() != reachable.size())
            throw new RuntimeException("Cycle detected in dependency graph!");

        return buildOrder;
    }

    // DFS to collect all reachable components
    private void discoverDFS(String pkg, Set<String> visited) {
        if (!visited.add(pkg)) return;

        for (String dep : getPackageBuildDependencies(pkg)) {
            discoverDFS(dep, visited);
        }
    }

    // ---------------------------------------------------------
    // TEST HARNESS
    // ---------------------------------------------------------
    public static void main(String[] args) {
        Map<String, List<String>> deps = new HashMap<>();
        deps.put("Service", Arrays.asList("Adapters", "Core", "Utils"));
        deps.put("Adapters", Arrays.asList("Interfaces"));
        deps.put("Core", Arrays.asList("Types"));
        deps.put("Utils", Collections.emptyList());
        deps.put("Interfaces", Collections.emptyList());
        deps.put("Types", Collections.emptyList());

        ServiceDiscovery resolver = new ServiceDiscovery(deps);

        List<String> order = resolver.dependencyResolver("Service");
        System.out.println(order);
    }
}
✅
 Time Complexity Analysis (Correct) 
Let: 
● V = number of reachable components (packages) 
 
● E = number of dependency edges among them 
 
1. Discovery Phase (discover) 
For every node, you call getPackageBuildDependencies once. 
Time = O(V + E) 
 
Why? 
● Each node visited once → O(V) 
 
● Each outgoing dependency edge explored once → O(E) 
 
2. Graph Construction Phase 
Double loop: 
for each comp in V: 
for each dependency in adjacency list: 
This touches each edge exactly once: 
Time = O(V + E) 
3. Kahn’s BFS 
Kahn's Algo is linear: 
Time = O(V + E) 
⇒ Total Time Complexity 
O(V + E)  +  O(V + E)  +  O(V + E)   
=  O(V + E) 
✔ Correct 
✅
 Space Complexity Analysis (Correct) 
1. Discovery set 
Set<String> allComponents → O(V) 
2. Adjacency List 
adjList → O(V + E) 
3. In-degree Map 
inOrder → O(V) 
4. BFS Queue 
Worst case: all nodes → O(V) 
⇒ Total Space = O(V + E) 
✔ Correct 
