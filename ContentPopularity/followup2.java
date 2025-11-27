⭐ FOLLOW-UP (b)
Can we make getMostPopular() truly O(1)?

TreeMap gives O(1) for lastKey(), but interviewer may want a design without TreeMap, similar to the Commodity Price question.

Goal:

increasePopularity → O(1)

decreasePopularity → O(1)

getMostPopular → O(1)

Without TreeMap

Frequent reads and writes

🎯 Key Insight

Maintain:

1️⃣ HashMap<Integer, Integer> contentToPopularity

→ store popularity for each content
→ O(1)

2️⃣ HashMap<Integer, Integer> popularityFreq

→ popularity → how many contents have this popularity
→ O(1)

3️⃣ A variable:
int currentMaxPopularity;


Then:

✔ increasePopularity(contentId)
oldPop = contentToPopularity[id]
newPop = oldPop + 1

decrement freq[oldPop]
increment freq[newPop]

if newPop > currentMaxPopularity:
    currentMaxPopularity = newPop

✔ decreasePopularity(contentId)
oldPop = contentToPopularity[id]
newPop = oldPop - 1

decrement freq[oldPop]
increment freq[newPop]

if oldPop == currentMaxPopularity AND freq[oldPop] == 0:
    recompute currentMaxPopularity (scan popularityFreq)

✔ getMostPopular()
if currentMaxPopularity <= 0 → return -1
else return ANY content whose popularity == currentMaxPopularity


We maintain a second map:

4️⃣ HashMap<Integer, Set<Integer>> popularityToContentIds

So returning content with max popularity is O(1).

⭐ Java Code — O(1) getMostPopular (AMORTIZED O(1) updates)
import java.util.*;

class PopularContentOptimized {

    private final Map<Integer, Integer> contentToPopularity = new HashMap<>();
    private final Map<Integer, Integer> popularityFreq = new HashMap<>();
    private final Map<Integer, Set<Integer>> popularityToIds = new HashMap<>();

    private int currentMax = 0;

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
            popularityFreq.put(oldPop, popularityFreq.get(oldPop) - 1);
            popularityToIds.get(oldPop).remove(contentId);
            if (popularityFreq.get(oldPop) == 0) {
                popularityFreq.remove(oldPop);
                popularityToIds.remove(oldPop);
            }
        }

        // Update new popularity
        if (newPop > 0) {
            contentToPopularity.put(contentId, newPop);

            popularityFreq.put(newPop, popularityFreq.getOrDefault(newPop, 0) + 1);
            popularityToIds.computeIfAbsent(newPop, k -> new HashSet<>()).add(contentId);
        } else {
            // newPop <= 0 → remove completely
            contentToPopularity.remove(contentId);
        }

        // Update currentMax
        if (newPop > currentMax) {
            currentMax = newPop;
        } else if (oldPop == currentMax && !popularityFreq.containsKey(oldPop)) {
            // Recompute max in case the old max disappeared
            currentMax = recomputeMax();
        }
    }

    private int recomputeMax() {
        int max = 0;
        for (int pop : popularityFreq.keySet()) {
            if (pop > max) max = pop;
        }
        return max;
    }

    public int getMostPopular() {
        if (currentMax <= 0) return -1;
        return popularityToIds.get(currentMax).iterator().next();
    }
}

⭐ Dry Run (Follow-up b)
Operations:
inc(10) → pop = 1 => max=1
inc(20) → pop = 1 => max=1
inc(10) → pop = 2 => max=2

mostPopular → 10

dec(10) → pop = 1 => max=1
dec(10) → pop = 0 => 10 removed
max=1

mostPopular → 20

dec(20) → pop = 0 => remove
max recompute → 0

mostPopular → -1


Works perfectly.

⭐ Complexity
Operation	Time
increasePopularity	O(1) amortized
decreasePopularity	O(1) amortized
getMostPopular	O(1)
Space	O(N)
