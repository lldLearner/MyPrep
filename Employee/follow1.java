/*Follow-up 1 — (a) Tree hierarchy (classic)

Goal: Given a set of employees, return the closest common parent group in a tree hierarchy (each group has exactly one parent except root). Employees can belong to one or more groups.

Approach (interview-style)

Model groups as nodes in a tree with parent pointers and depths.

Map employee → set of groups they belong to.

For each employee, gather the groups they belong to. For each such group, walk up to the root to collect its ancestor chain (or use parent pointers + depth).

Reduce to a common node by computing Lowest Common Ancestor (LCA).

If employees map to multiple groups each, compute LCA pairwise: pick one representative group for the first employee, then for every other employee compute LCA of current result with that employee’s candidate groups (for multiple groups per employee, treat as multiple possible LCAs — compute union of ancestors for that employee then intersect).

Simpler deterministic algorithm for tree:

For each employee, build set of ancestors for all their groups (union of ancestors per group).

Intersect all employees’ ancestor sets → set of common ancestors.

Choose the deepest (max depth) ancestor → closest common group.

Tie-break deterministically (e.g., lexicographically).

This is straightforward and efficient when the hierarchy is a tree because ancestor relationships are unique.

Dry run (small)

Groups: A(root)->B->D, A->C->E
Employees:

emp1 in D

emp2 in E

emp3 in B

Ancestors:

D: {D,B,A}

E: {E,C,A}

B: {B,A}
Intersection = {A} → return A.

Complexity

Build ancestors for a group: O(height). Precompute for all groups: O(N * height) worst-case.

Query: for each employee union ancestors of their groups (sum sizes) and intersections. If G groups, total cost roughly O(sum of ancestor-set sizes). In tree with precomputed ancestor sets: O(totalGroupsVisited) per query.

Space: O(#groups * avg ancestor set size) if precomputed.

Runnable Java (single-file)

Save as Followup1_TreeLCA.java and run:
javac Followup1_TreeLCA.java && java Followup1_TreeLCA
*/
import java.util.*;

/**
 * Followup1_TreeLCA.java
 * Simple tree-based LCA via ancestor sets union & intersection.
 */
public class Followup1_TreeLCA {

    static class Group {
        String id;
        String parent; // single parent (null if root)
        int depth;
        Group(String id, String parent) { this.id = id; this.parent = parent; this.depth = -1; }
    }

    static class Org {
        Map<String, Group> groups = new HashMap<>();
        Map<String, Set<String>> empToGroups = new HashMap<>();
        Map<String, Set<String>> ancestors = new HashMap<>();

        void addGroup(String id, String parent) {
            groups.putIfAbsent(id, new Group(id, parent));
        }
        void addEmployeeToGroup(String emp, String groupId) {
            empToGroups.computeIfAbsent(emp, k -> new HashSet<>()).add(groupId);
        }
        void buildAncestorsAndDepths() {
            for (String g : groups.keySet()) {
                computeAncestors(g);
            }
            for (Map.Entry<String, Set<String>> e : ancestors.entrySet()) {
                // depth = size-1 measured relative to root (simple)
                groups.get(e.getKey()).depth = e.getValue().size()-1;
            }
        }
        private Set<String> computeAncestors(String gid) {
            if (ancestors.containsKey(gid)) return ancestors.get(gid);
            Set<String> res = new LinkedHashSet<>();
            String cur = gid;
            while (cur != null) {
                res.add(cur);
                Group g = groups.get(cur);
                if (g == null) break;
                cur = g.parent;
            }
            ancestors.put(gid, Collections.unmodifiableSet(res));
            return res;
        }

        // main query
        String closestCommonGroup(Set<String> employees) {
            if (employees == null || employees.isEmpty()) return null;
            Iterator<String> it = employees.iterator();
            Set<String> inter = null;
            while (it.hasNext()) {
                String emp = it.next();
                Set<String> groupsForEmp = empToGroups.getOrDefault(emp, Collections.emptySet());
                Set<String> unionAnc = new HashSet<>();
                for (String g : groupsForEmp) {
                    Set<String> a = ancestors.getOrDefault(g, Collections.emptySet());
                    unionAnc.addAll(a);
                }
                if (inter == null) inter = new HashSet<>(unionAnc);
                else inter.retainAll(unionAnc);
                if (inter.isEmpty()) return null;
            }
            // choose deepest (max depth)
            String best = null;
            int bestDepth = Integer.MIN_VALUE;
            for (String cand : inter) {
                int d = groups.get(cand).depth;
                if (d > bestDepth || (d == bestDepth && (best == null || cand.compareTo(best) < 0))) {
                    best = cand; bestDepth = d;
                }
            }
            return best;
        }
    }

    // runnable demo
    public static void main(String[] args) {
        Org org = new Org();
        // build tree: A->B->D and A->C->E
        org.addGroup("A", null);
        org.addGroup("B", "A");
        org.addGroup("C", "A");
        org.addGroup("D", "B");
        org.addGroup("E", "C");

        org.addEmployeeToGroup("emp1", "D");
        org.addEmployeeToGroup("emp2", "E");
        org.addEmployeeToGroup("emp3", "B");

        org.buildAncestorsAndDepths();

        Set<String> query = new HashSet<>(Arrays.asList("emp1", "emp2", "emp3"));
        String ans = org.closestCommonGroup(query);
        System.out.println("Closest common group for " + query + " => " + ans);
        // expected: A
    }
}
