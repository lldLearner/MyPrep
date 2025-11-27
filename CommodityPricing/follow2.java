⭐ FOLLOW-UP (b)
Can we make getMaxCommodityPrice() truly O(1)?

Yes.
TreeMap gives O(1) for max key, but if interviewer wants no TreeMap, then:

Maintain:

int currentMaxPrice

During upsert:

Case 1: newPrice > currentMaxPrice

→ just update currentMaxPrice

Case 2: updating timestamp whose old price == currentMaxPrice

→ frequency reduces
→ if frequency becomes 0
→ we must recalculate max by scanning freq map

This is O(N) but happens rarely → amortized O(1).

⭐ FOLLOW-UP (b) JAVA CODE (O(1) getMax)**
import java.util.*;

class RunningCommodityPriceOptimized {

    private final Map<Integer, Integer> tsToPrice = new HashMap<>();
    private final Map<Integer, Integer> priceFreq = new HashMap<>();

    private int maxPrice = Integer.MIN_VALUE;

    public void upsertCommodityPrice(int timestamp, int price) {

        // Remove old price if exists
        if (tsToPrice.containsKey(timestamp)) {
            int oldPrice = tsToPrice.get(timestamp);
            priceFreq.put(oldPrice, priceFreq.get(oldPrice) - 1);

            // If old max lost its last occurrence → recompute
            if (oldPrice == maxPrice && priceFreq.get(oldPrice) == 0) {
                maxPrice = recomputeMaxPrice();
            }
        }

        // Insert new price
        tsToPrice.put(timestamp, price);
        priceFreq.put(price, priceFreq.getOrDefault(price, 0) + 1);

        // Update maxPrice O(1)
        if (price > maxPrice) {
            maxPrice = price;
        }
    }

    private int recomputeMaxPrice() {
        int newMax = Integer.MIN_VALUE;
        for (int p : priceFreq.keySet()) {
            if (priceFreq.get(p) > 0) newMax = Math.max(newMax, p);
        }
        return newMax;
    }

    public int getMaxCommodityPrice() {
        if (tsToPrice.isEmpty()) return -1;
        return maxPrice;
    }
}
