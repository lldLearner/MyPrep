⭐ FOLLOW-UP (a) — Basic Problem
Implement a class that supports:

increasePopularity(contentId)

decreasePopularity(contentId)

getMostPopular()

Rules:

popularities can go up & down

if popularity of all content ≤ 0 → return -1

ties → return any content with highest popularity (or smallest id if interviewer wants determinism)

frequent reads & writes

✅ Approach (Interview Style)

We need:

1. HashMap<Integer,Integer> contentToPopularity

Stores current popularity for each contentId
→ O(1) updates

2. TreeMap<Integer, Set<Integer>> popularityToContents

Key = popularity
Value = set of contentIds with that popularity
→ allows us to get max popularity in O(1) via lastKey()

3. Whenever popularity changes:

If content moved from oldPopularity to newPopularity:

remove contentId from popularityToContents[oldPopularity]
if set becomes empty → remove that entry
add contentId to popularityToContents[newPopularity]

4. getMostPopular():

if lastKey() <= 0 → return -1

else return any contentId from popularityToContents.get(lastKey())

⭐ FOLLOW-UP (a) — JAVA CODE
import java.util.*;

class PopularContent {

    private final Map<Integer, Integer> contentToPopularity = new HashMap<>();
    private final TreeMap<Integer, Set<Integer>> popularityToIds = new TreeMap<>();

    public void increasePopularity(int contentId) {
        update(contentId, +1);
    }

    public void decreasePopularity(int contentId) {
        update(contentId, -1);
    }

    private void update(int contentId, int delta) {
        int oldPop = contentToPopularity.getOrDefault(contentId, 0);
        int newPop = oldPop + delta;

        // Remove from old popularity bucket
        if (oldPop != 0) {
            Set<Integer> bucket = popularityToIds.get(oldPop);
            bucket.remove(contentId);
            if (bucket.isEmpty()) popularityToIds.remove(oldPop);
        }

        // Add to new popularity bucket (if non-zero)
        if (newPop != 0) {
            popularityToIds.computeIfAbsent(newPop, k -> new HashSet<>()).add(contentId);
        }

        // Update map
        if (newPop == 0) contentToPopularity.remove(contentId);
        else contentToPopularity.put(contentId, newPop);
    }

    public int getMostPopular() {
        if (popularityToIds.isEmpty()) return -1;

        int maxPopularity = popularityToIds.lastKey();
        if (maxPopularity <= 0) return -1;

        // return ANY id from that popularity bucket
        return popularityToIds.get(maxPopularity).iterator().next();
    }
}

⭐ Example MAIN for testing
public class Main {
    public static void main(String[] args) {
        PopularContent p = new PopularContent();

        p.increasePopularity(10);    // pop(10) = 1
        p.increasePopularity(20);    // pop(20) = 1
        p.increasePopularity(10);    // pop(10) = 2

        System.out.println(p.getMostPopular());  // 10

        p.decreasePopularity(10);   // pop(10) = 1
        p.decreasePopularity(10);   // pop(10) = 0

        System.out.println(p.getMostPopular());  // 20

        p.decreasePopularity(20);   // pop(20) = 0
        System.out.println(p.getMostPopular());  // -1
    }
}

⭐ Dry Run

Actions:

+1 on 10 → pop=1
+1 on 20 → pop=1
+1 on 10 → pop=2
Most: 10 (pop=2)

-1 on 10 → pop=1
-1 on 10 → pop=0
Most: 20 (pop=1)

-1 on 20 → pop=0
Most: -1

⭐ Complexity
Operation	Complexity
increasePopularity	O(log N)
decreasePopularity	O(log N)
getMostPopular	O(1)
Space	O(N)

TreeMap logN operations are totally fine for real systems and interview acceptance.
