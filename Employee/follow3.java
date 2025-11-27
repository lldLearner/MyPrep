/*Follow-up 3 — (c) Concurrency: concurrent updates + reads must see latest consistent view

Goal: There are 4 update methods (add/remove group, add/remove employee) executed concurrently; getCommonGroupForEmployees must always reflect the latest updated state of the hierarchy — reads must be consistent and efficient.

Approach (interview-style)

Two mainstream approaches:

Copy-on-Write Snapshot (AtomicReference) — recommended for interview:

Maintain an immutable OrgSnapshot (maps, precomputed ancestors, depths).

Writers create a new snapshot by copying & modifying structure then compareAndSet swap into AtomicReference.

Readers call snapshotRef.get() — lock-free, immediate, consistent view (either before or after any writer).

Pros: Reads are super fast and non-blocking. Strong consistency w.r.t snapshot swap (reads atomic).

Cons: Writes heavier (copying). Suitable if writes are moderate vs reads heavy.

Read-Write Lock with in-place mutation:

ReadWriteLock allows multiple concurrent readers; writer uses exclusive lock.

Pros: Less copying on writes.

Cons: Writers block readers; potential contention.

For the requirement "always reflects the latest updated state" — snapshot swap gives linearizability at swap point; if you need readers to always see most recent write even during long-running read, snapshot ensures read sees either pre or post, never a half-updated state.

Design details for snapshot approach:

AtomicReference<OrgSnapshot> snapshotRef.

Each writer:

while(true) { OrgSnapshot old = snapshotRef.get(); OrgSnapshot modified = old.applyChange(...); if (snapshotRef.compareAndSet(old, modified)) break; }

applyChange returns a new snapshot with updated group map, employee->groups and recomputed memo (ancestors/depth).

Readers:

Get snapshot once and operate on it — consistent, immutable.

Handling concurrent writers and conflicts:

CAS loop ensures one wins; other retries using latest snapshot (lack of locks).

For complex multi-step updates, you may need transactional logic at higher layer.

Dry run

Reader R reads snapshot S1.

Writer W1 swaps S2.

Reader R still sees S1 (consistent). New reads after swap see S2.

No reader sees partially updated mix.

Runnable Java (single-file) — simplified org with snapshot and 4 update methods

Save as Followup3_SnapshotConcurrency.java. Compile & run:
javac Followup3_SnapshotConcurrency.java && java Followup3_SnapshotConcurrency*/

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Followup3_SnapshotConcurrency.java
 * Demonstrates snapshot (immutable) approach with CAS for updates and lock-free reads.
 */
public class Followup3_SnapshotConcurrency {

    static class Group {
        final String id;
        final Set<String> parents;
        final Set<String> children;
        final Set<String> employees;
        Group(String id, Set<String> parents, Set<String> children, Set<String> employees) {
            this.id = id;
            this.parents = Collections.unmodifiableSet(new HashSet<>(parents));
            this.children = Collections.unmodifiableSet(new HashSet<>(children));
            this.employees = Collections.unmodifiableSet(new HashSet<>(employees));
        }
    }

    static class OrgSnapshot {
        final Map<String, Group> groups;
        final Map<String, Set<String>> empToGroups;
        final Map<String, Set<String>> ancestors;
        final Map<String, Integer> depth;
        OrgSnapshot(Map<String, Group> groups, Map<String, Set<String>> empToGroups,
                    Map<String, Set<String>> ancestors, Map<String, Integer> depth) {
            this.groups = Collections.unmodifiableMap(new HashMap<>(groups));
            Map<String, Set<String>> tmp = new HashMap<>();
            for (Map.Entry<String, Set<String>> e : empToGroups.entrySet())
                tmp.put(e.getKey(), Collections.unmodifiableSet(new HashSet<>(e.getValue())));
            this.empToGroups = Collections.unmodifiableMap(tmp);
            this.ancestors = Collections.unmodifiableMap(new HashMap<>(ancestors));
            this.depth = Collections.unmodifiableMap(new HashMap<>(depth));
        }
    }

    static class OrgManager {
        private final AtomicReference<OrgSnapshot> ref;
        public OrgManager() {
            ref = new AtomicReference<>(new OrgSnapshot(new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>()));
        }

        // read method (fast, lock-free)
        public String getClosestCommon(Set<String> emps) {
            OrgSnapshot snap = ref.get();
            if (emps == null || emps.isEmpty()) return null;
            Iterator<String> it = emps.iterator();
            Set<String> inter = null;
            while (it.hasNext()) {
                String e = it.next();
                Set<String> groupsFor = snap.empToGroups.getOrDefault(e, Collections.emptySet());
                Set<String> unionAnc = new HashSet<>();
                for (String g : groupsFor) unionAnc.addAll(snap.ancestors.getOrDefault(g, Collections.emptySet()));
                if (inter == null) inter = new HashSet<>(unionAnc);
                else inter.retainAll(unionAnc);
                if (inter.isEmpty()) return null;
            }
            String best = null; int bestDepth = Integer.MIN_VALUE;
            for (String c : inter) {
                int d = snap.depth.getOrDefault(c, 0);
                if (d > bestDepth || (d==bestDepth && (best==null || c.compareTo(best)<0))) { best = c; bestDepth = d; }
            }
            return best;
        }

        // helper: atomic CAS update by providing a mutator that receives old snapshot maps and returns modified maps
        private void casUpdate(Updater updater) {
            while (true) {
                OrgSnapshot oldSnap = ref.get();
                // copy current maps to mutable
                Map<String, Group> groups = new HashMap<>(oldSnap.groups);
                Map<String, Set<String>> empToGroups = new HashMap<>();
                for (Map.Entry<String, Set<String>> e : oldSnap.empToGroups.entrySet())
                    empToGroups.put(e.getKey(), new HashSet<>(e.getValue()));
                // apply update
                updater.apply(groups, empToGroups);
                // rebuild ancestor & depth memo (simple full recompute)
                Map<String, Set<String>> ancestors = buildAncestors(groups);
                Map<String, Integer> depth = buildDepthsFromAncestors(ancestors);
                OrgSnapshot newSnap = new OrgSnapshot(groups, empToGroups, ancestors, depth);
                if (ref.compareAndSet(oldSnap, newSnap)) break; // success
                // else retry
            }
        }

        // four updates
        public void addGroup(String gid, Set<String> parents) {
            casUpdate((groups, e2g) -> {
                groups.putIfAbsent(gid, new Group(gid, parents == null ? new HashSet<>() : parents, new HashSet<>(), new HashSet<>()));
                if (parents != null) {
                    for (String p : parents) {
                        groups.putIfAbsent(p, new Group(p, new HashSet<>(), new HashSet<>(), new HashSet<>()));
                        // update child link
                        Group pg = groups.get(p);
                        Set<String> newChildren = new HashSet<>(pg.children); newChildren.add(gid);
                        groups.put(p, new Group(p, pg.parents, newChildren, pg.employees));
                    }
                }
            });
        }
        public void removeGroup(String gid) {
            casUpdate((groups, e2g) -> {
                if (!groups.containsKey(gid)) return;
                // remove group, remove from parents/children, remove employees membership
                Group g = groups.remove(gid);
                for (String p : g.parents) { if (groups.containsKey(p)) {
                    Group pg = groups.get(p);
                    Set<String> newChildren = new HashSet<>(pg.children); newChildren.remove(gid);
                    groups.put(p, new Group(p, pg.parents, newChildren, pg.employees));
                } }
                for (String c : g.children) { if (groups.containsKey(c)) {
                    Group cg = groups.get(c);
                    Set<String> newParents = new HashSet<>(cg.parents); newParents.remove(gid);
                    groups.put(c, new Group(c, newParents, cg.children, cg.employees));
                } }
                // remove group from employees map
                for (Set<String> s : e2g.values()) s.remove(gid);
            });
        }
        public void addEmployeeToGroup(String emp, String gid) {
            casUpdate((groups, e2g) -> {
                if (!groups.containsKey(gid)) groups.put(gid, new Group(gid, new HashSet<>(), new HashSet<>(), new HashSet<>()));
                e2g.computeIfAbsent(emp, k-> new HashSet<>()).add(gid);
                // update group employees
                Group g = groups.get(gid);
                Set<String> newEmp = new HashSet<>(g.employees); newEmp.add(emp);
                groups.put(gid, new Group(gid, g.parents, g.children, newEmp));
            });
        }
        public void removeEmployeeFromGroup(String emp, String gid) {
            casUpdate((groups, e2g) -> {
                Set<String> s = e2g.get(emp);
                if (s != null) s.remove(gid);
                Group g = groups.get(gid);
                if (g != null) {
                    Set<String> newEmp = new HashSet<>(g.employees); newEmp.remove(emp);
                    groups.put(gid, new Group(gid, g.parents, g.children, newEmp));
                }
            });
        }

        // helpers to rebuild memos
        private static Map<String, Set<String>> buildAncestors(Map<String, Group> groups) {
            Map<String, Set<String>> memo = new HashMap<>();
            for (String g : groups.keySet()) computeAnc(memo, groups, g);
            return memo;
        }
        private static Set<String> computeAnc(Map<String, Set<String>> memo, Map<String, Group> groups, String g) {
            if (memo.containsKey(g)) return memo.get(g);
            Set<String> res = new HashSet<>();
            res.add(g);
            Group grp = groups.get(g);
            if (grp != null) for (String p : grp.parents) res.addAll(computeAnc(memo, groups, p));
            memo.put(g, Collections.unmodifiableSet(res));
            return memo.get(g);
        }
        private static Map<String, Integer> buildDepthsFromAncestors(Map<String, Set<String>> anc) {
            Map<String, Integer> d = new HashMap<>();
            for (Map.Entry<String, Set<String>> e : anc.entrySet()) d.put(e.getKey(), e.getValue().size()-1);
            return d;
        }

        // functional updater
        private interface Updater { void apply(Map<String, Group> groups, Map<String, Set<String>> empToGroups); }
    }

    // demo with concurrency
    public static void main(String[] args) throws Exception {
        OrgManager manager = new OrgManager();

        // initial
        manager.addGroup("A", null);
        manager.addGroup("B", new HashSet<>(Arrays.asList("A")));
        manager.addGroup("C", new HashSet<>(Arrays.asList("A")));
        manager.addEmployeeToGroup("u1","B");
        manager.addEmployeeToGroup("u2","C");

        // spawn a writer thread that will add a deeper group after a short sleep
        ExecutorService ex = Executors.newFixedThreadPool(2);
        ex.submit(() -> {
            try { Thread.sleep(100); } catch (InterruptedException e) {}
            manager.addGroup("D", new HashSet<>(Arrays.asList("B")));
            manager.addEmployeeToGroup("u1","D"); // move u1 also in D
            System.out.println("[writer] added D and assigned u1 to D");
        });

        // reader repeatedly reads; may see pre or post snapshot, always consistent
        ex.submit(() -> {
            for (int i=0;i<6;i++) {
                String res = manager.getClosestCommon(new HashSet<>(Arrays.asList("u1","u2")));
                System.out.println("[reader] query => " + res);
                try { Thread.sleep(80); } catch (InterruptedException e) {}
            }
        });

        ex.shutdown();
        ex.awaitTermination(2, TimeUnit.SECONDS);
    }
}


Notes: This snapshot approach gives lock-free reads and atomic updates. Writers retry on CAS failure. In production you'd optimize partial rebuild of memo instead of full recompute.
