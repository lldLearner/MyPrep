/*Follow-up 4 — (d) Single-level groups (no subgroups) — simplest case

Goal: Company only has groups with no subgroups; each group contains a set of employees. Find a single group that contains all target employees. Fast and trivial.

Approach (interview-style)

For each employee, have the set of groups they belong to.

Intersect across employees to get groups that contain all of them (because there are no ancestors).

If intersection non-empty, choose deterministic one (e.g., group with smallest size to be "closest"? but candidate definition is trivial — pick any or pick lexicographically smallest).

Complexity: O(sum of group counts for the employees) — very fast.

Dry run

Groups: G1:{a,b,c}, G2:{b,c,d}, G3:{a,d}
Query employees {b,c}:

emp b groups = {G1,G2}

emp c groups = {G1,G2}

intersection = {G1,G2} → choose lexicographic -> G1.

Runnable Java (single-file)

Save as Followup4_SingleLevel.java. Run:
javac Followup4_SingleLevel.java && java Followup4_SingleLevel
*/
import java.util.*;

/**
 * Followup4_SingleLevel.java
 * Simple intersection over employee->groups mapping.
 */
public class Followup4_SingleLevel {
    static class Org {
        Map<String, Set<String>> empToGroups = new HashMap<>();
        void addGroupWithEmployees(String group, Set<String> emps) {
            for (String e : emps) empToGroups.computeIfAbsent(e, k-> new HashSet<>()).add(group);
        }
        String findGroupContainingAll(Set<String> emps) {
            if (emps == null || emps.isEmpty()) return null;
            Iterator<String> it = emps.iterator();
            Set<String> inter = null;
            while (it.hasNext()) {
                String e = it.next();
                Set<String> gs = empToGroups.getOrDefault(e, Collections.emptySet());
                if (inter == null) inter = new HashSet<>(gs);
                else inter.retainAll(gs);
                if (inter.isEmpty()) return null;
            }
            // pick deterministic
            return inter.stream().min(String::compareTo).orElse(null);
        }
    }

    public static void main(String[] args) {
        Org o = new Org();
        o.addGroupWithEmployees("G1", new HashSet<>(Arrays.asList("a","b","c")));
        o.addGroupWithEmployees("G2", new HashSet<>(Arrays.asList("b","c","d")));
        o.addGroupWithEmployees("G3", new HashSet<>(Arrays.asList("a","d")));

        System.out.println(o.findGroupContainingAll(new HashSet<>(Arrays.asList("b","c")))); // G1
        System.out.println(o.findGroupContainingAll(new HashSet<>(Arrays.asList("a","d")))); // G3
        System.out.println(o.findGroupContainingAll(new HashSet<>(Arrays.asList("a","x")))); // null
    }
}
