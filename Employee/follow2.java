/*Follow-up 2 — (b) DAG / shared groups (one closest common group required)

Goal: Hierarchy may become a DAG: groups can have multiple parents, and employees can be in multiple groups. Requirement: return one closest common group.

Approach (interview-style)

Use same idea as tree but ancestors are computed in DAG (multiple parents).

For each group compute ancestor set (including itself) via DFS with memoization to avoid exponential blowup.

For each employee, form the union of ancestor sets of groups they directly belong to.

Intersect across employees → common ancestors.

Choose the closest ancestor defined by maximum depth, where depth is defined as size of ancestor set - 1 OR better: longest path length from that node to any root (precompute longest distance-to-root using DP on DAG).

If multiple tie, apply deterministic tie breaker (lexicographic, creation timestamp, or group id).

Complexity: ancestor computation with memoization is O(V+E) overall for all groups.

Dry run

Groups DAG:

G1 -> parent P1, P2 (two parents)

Employees mapped similarly; intersection logic remains same.

If intersection yields multiple nodes at same max depth, return lexicographically smallest ID where tie exists.

Runnable Java (single-file)

Save as Followup2_DAGClosest.java. Run:
javac Followup2_DAGClosest.java && java Followup2_DAGClosest
*/
import java.util.*;

/**
 * Followup2_DAGClosest.java
 * DAG ancestor computation with memoization and depth (longest path to root).
 */
public class Followup2_DAGClosest {

    static class Group {
        String id;
        Set<String> parents = new HashSet<>();
        Set<String> children = new HashSet<>();
        Group(String id) { this.id = id; }
    }

    static class Org {
        Map<String, Group> groups = new HashMap<>();
        Map<String, Set<String>> empToGroups = new HashMap<>();
        Map<String, Set<String>> ancestorsMemo = new HashMap<>();
        Map<String, Integer> depthMemo = new HashMap<>(); // longest path length to a root

        void addGroup(String id) { groups.putIfAbsent(id, new Group(id)); }
        void addParent(String child, String parent) {
            addGroup(child); addGroup(parent);
            groups.get(child).parents.add(parent);
            groups.get(parent).children.add(child);
        }
        void addEmployeeToGroup(String emp, String group) {
            empToGroups.computeIfAbsent(emp, k-> new HashSet<>()).add(group);
        }

        void buildMemo() {
            // ancestors
            for (String g : groups.keySet()) {
                computeAncestors(g);
            }
            // depth = longest path to a root (root has depth 0)
            for (String g : groups.keySet()) {
                depthMemo.put(g, computeDepth(g));
            }
        }

        private Set<String> computeAncestors(String gid) {
            if (ancestorsMemo.containsKey(gid)) return ancestorsMemo.get(gid);
            Set<String> res = new HashSet<>();
            res.add(gid);
            for (String p : groups.get(gid).parents) {
                res.addAll(computeAncestors(p));
            }
            ancestorsMemo.put(gid, Collections.unmodifiableSet(res));
            return res;
        }

        private int computeDepth(String gid) {
            if (depthMemo.containsKey(gid)) return depthMemo.get(gid);
            int best = 0;
            for (String p : groups.get(gid).parents) {
                best = Math.max(best, 1 + computeDepth(p));
            }
            depthMemo.put(gid, best);
            return best;
        }

        String closestCommon(Set<String> employees) {
            if (employees == null || employees.isEmpty()) return null;
            Iterator<String> it = employees.iterator();
            Set<String> inter = null;
            while (it.hasNext()) {
                String emp = it.next();
                Set<String> groupsFor = empToGroups.getOrDefault(emp, Collections.emptySet());
                Set<String> unionAnc = new HashSet<>();
                for (String g : groupsFor) {
                    unionAnc.addAll(ancestorsMemo.getOrDefault(g, Collections.emptySet()));
                }
                if (inter == null) inter = new HashSet<>(unionAnc);
                else inter.retainAll(unionAnc);
                if (inter.isEmpty()) return null;
            }
            String best = null; int bestDepth = Integer.MIN_VALUE;
            for (String cand : inter) {
                int d = depthMemo.getOrDefault(cand, 0);
                if (d > bestDepth || (d == bestDepth && (best == null || cand.compareTo(best) < 0))) {
                    best = cand; bestDepth = d;
                }
            }
            return best;
        }
    }

    // demo
    public static void main(String[] args) {
        Org org = new Org();
        // DAG: R1 and R2 are roots; A has parents R1,R2
        org.addGroup("R1"); org.addGroup("R2");
        org.addParent("A", "R1");
        org.addParent("A", "R2");
        org.addParent("B", "R1");
        org.addParent("C", "A");
        org.addParent("D", "B");

        org.addEmployeeToGroup("e1", "C"); // path C->A->{R1,R2}
        org.addEmployeeToGroup("e2", "D"); // path D->B->R1

        org.buildMemo();

        Set<String> q = new HashSet<>(Arrays.asList("e1","e2"));
        System.out.println("Closest common for " + q + " => " + org.closestCommon(q));
        // expected: R1 (common ancestor), chosen because depth(R1)=0 vs R2=0 but lexicographic tie -> R1
    }
}
