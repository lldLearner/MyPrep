import java.util.*;
import java.util.concurrent.*;

public class InvertedIndexMultithreaded {

    private final Map<String, List<Integer>> index = new HashMap<>();

    // ----------------- Indexing -----------------
    public void indexMessage(int msgId, String message) {
        String[] words = message.toLowerCase().split("\\W+");

        for (String w : words) {
            index.computeIfAbsent(w, k -> new ArrayList<>()).add(msgId);
        }
    }

    // ----------------- Multi-threaded Search -----------------
    public List<Integer> searchMultipleParallel(String... keywords) throws Exception {
        if (keywords.length == 0) return List.of();

        ExecutorService pool = Executors.newFixedThreadPool(Math.min(8, keywords.length));

        // ---- Step 1: Fetch posting lists in parallel ----
        List<Future<List<Integer>>> futures = new ArrayList<>();

        for (String k : keywords) {
            futures.add(pool.submit(() ->
                index.getOrDefault(k.toLowerCase(), Collections.emptyList())
            ));
        }

        List<List<Integer>> postingLists = new ArrayList<>();
        for (Future<List<Integer>> f : futures) {
            postingLists.add(f.get());
        }

        // shutdown fetch threads (posting lists loaded)
        pool.shutdown();

        // ---- Step 2: Sort posting lists by size ----
        postingLists.sort(Comparator.comparingInt(List::size));

        // ---- Step 3: Parallel intersection ----
        return parallelIntersect(postingLists);
    }

    // ----------------- Parallel Intersection -----------------
    private List<Integer> parallelIntersect(List<List<Integer>> lists) throws Exception {
        if (lists.size() == 1) return lists.get(0);

        ExecutorService pool = Executors.newFixedThreadPool(Math.min(8, lists.size()));

        List<List<Integer>> current = lists;

        while (current.size() > 1) {
            List<Future<List<Integer>>> futures = new ArrayList<>();

            for (int i = 0; i < current.size(); i += 2) {
                List<Integer> a = current.get(i);
                List<Integer> b = (i + 1 < current.size()) ? current.get(i + 1) : null;

                futures.add(pool.submit(() -> {
                    if (b == null) return a;
                    return intersect(a, b);
                }));
            }

            List<List<Integer>> nextRound = new ArrayList<>();
            for (Future<List<Integer>> f : futures) {
                nextRound.add(f.get());
            }
            current = nextRound;
        }

        pool.shutdown();
        return current.get(0);
    }

    // Two-pointer list intersection
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

    // ----------------- MAIN -----------------
    public static void main(String[] args) throws Exception {
        InvertedIndexMultithreaded idx = new InvertedIndexMultithreaded();

        idx.indexMessage(1, "disk fatal error");
        idx.indexMessage(2, "this is a disk issue");
        idx.indexMessage(3, "fatal disk error occurs");
        idx.indexMessage(4, "warning warning disk is full");
        idx.indexMessage(5, "fatal system failure error disk crash");

        System.out.println("Parallel search: fatal AND disk AND error");

        List<Integer> result = idx.searchMultipleParallel("fatal", "disk", "error");

        System.out.println("Result = " + result); // Expect something like [1, 3, 5]
    }
}
