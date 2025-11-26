import java.util.*;

public class InvertedIndex {

    // keyword → sorted list of message IDs
    private final Map<String, List<Integer>> invertedIndex = new HashMap<>();


    // ---------- Task 2: Index a Message ----------
    public void indexMessage(int msgId, String message) {
        // tokenize message (simple splitter)
        String[] words = message.toLowerCase().split("\\W+");

        for (String w : words) {
            if (w.isBlank()) continue;

            List<Integer> postingList =
                    invertedIndex.computeIfAbsent(w, k -> new ArrayList<>());

            postingList.add(msgId);  // append (IDs come in increasing order)
        }
    }


    // ---------- Task 3: Search a Single Keyword ----------
    public List<Integer> searchSingle(String keyword) {
        return invertedIndex.getOrDefault(
                keyword.toLowerCase(),
                Collections.emptyList()
        );
    }


    // ---------- Task 4: Search Multiple Keywords (AND query) ----------
    public List<Integer> searchMultiple(String... keywords) {
        if (keywords.length == 0) return List.of();

        // get posting lists for each keyword
        List<List<Integer>> lists = new ArrayList<>();
        for (String k : keywords) {
            lists.add(searchSingle(k));
        }

        // sort by length → smallest first (optimization!)
        lists.sort(Comparator.comparingInt(List::size));

        // start from smallest list
        List<Integer> result = new ArrayList<>(lists.get(0));

        // intersect with each remaining list
        for (int i = 1; i < lists.size(); i++) {
            result = intersect(result, lists.get(i));
            if (result.isEmpty()) break;
        }

        return result;
    }


    // ---------- Utility: Sorted List Intersection ----------
    private List<Integer> intersect(List<Integer> a, List<Integer> b) {
        List<Integer> out = new ArrayList<>();
        int i = 0, j = 0;

        while (i < a.size() && j < b.size()) {
            int x = a.get(i), y = b.get(j);

            if (x == y) {
                out.add(x);
                i++; j++;
            } else if (x < y) {
                i++;
            } else {
                j++;
            }
        }
        return out;
    }


    // ---------- MAIN: Full Example ----------
    public static void main(String[] args) {
        InvertedIndex index = new InvertedIndex();

        // Index some messages
        index.indexMessage(1, "disk fatal error");
        index.indexMessage(2, "this is a disk issue");
        index.indexMessage(3, "fatal disk error occurred in the system");
        index.indexMessage(4, "warning warning disk is full");
        index.indexMessage(5, "fatal system failure error disk crash");

        // Single keyword search
        System.out.println("Messages containing 'disk':");
        System.out.println(index.searchSingle("disk"));  
        // Output: [1, 2, 3, 4, 5]

        // Multi-keyword: AND search
        System.out.println("\nMessages containing: fatal AND disk");
        System.out.println(index.searchMultiple("fatal", "disk"));
        // Possible Output: [1, 3, 5]

        // Multi-keyword: many frequent keywords
        System.out.println("\nMessages containing: fatal AND disk AND error");
        System.out.println(index.searchMultiple("fatal", "disk", "error"));
        // Possible Output: [1, 3, 5]
    }
}
