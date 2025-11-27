✅ Approach (Interview Style – Java):

We need 2 data structures:

HashMap<Integer,Integer> timestampToPrice
→ Stores latest price per timestamp
→ O(1) upsert

TreeMap<Integer,Integer> priceFrequency
→ Key = price
→ Value = how many timestamps currently have that price
→ TreeMap gives us max key in O(1) using lastKey()

So:

upsert:

Check if timestamp has old price → decrement its freq

Insert new price → increment freq

Update maps

getMaxCommodityPrice():

return priceFrequency.lastKey()

Time Complexity:

upsert: O(log N)

getMax: O(1)

✅ FOLLOW-UP (a) JAVA CODE
import java.util.*;

class RunningCommodityPrice {

    private final Map<Integer, Integer> tsToPrice = new HashMap<>();
    private final TreeMap<Integer, Integer> priceFreq = new TreeMap<>();

    // Upsert operation
    public void upsertCommodityPrice(int timestamp, int price) {

        // Case 1: timestamp already exists → decrement old price
        if (tsToPrice.containsKey(timestamp)) {
            int oldPrice = tsToPrice.get(timestamp);
            int freq = priceFreq.get(oldPrice);

            if (freq == 1) priceFreq.remove(oldPrice);
            else priceFreq.put(oldPrice, freq - 1);
        }

        // Case 2: insert/update new price
        tsToPrice.put(timestamp, price);
        priceFreq.put(price, priceFreq.getOrDefault(price, 0) + 1);
    }

    // Get current max commodity price
    public int getMaxCommodityPrice() {
        if (priceFreq.isEmpty()) return -1;
        return priceFreq.lastKey();
    }
}

public class FollowupA {
    public static void main(String[] args) {

        RunningCommodityPrice r = new RunningCommodityPrice();

        r.upsertCommodityPrice(4, 27);
        r.upsertCommodityPrice(6, 26);
        r.upsertCommodityPrice(9, 27);

        System.out.println(r.getMaxCommodityPrice());  // 27

        r.upsertCommodityPrice(4, 28); // timestamp 4 updated

        System.out.println(r.getMaxCommodityPrice());  // 28
    }
}
