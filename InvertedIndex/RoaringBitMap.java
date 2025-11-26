import org.roaringbitmap.RoaringBitmap;
import java.util.*;

public class BitmapInvertedIndex {

    private final Map<String, RoaringBitmap> index = new HashMap<>();

    // Task 2: index a message
    public void indexMessage(int msgId, String message) {
        String[] words = message.toLowerCase().split("\\W+");

        for (String w : words) {
            index.computeIfAbsent(w, k -> new RoaringBitmap())
                 .add(msgId);
        }
    }

    // Task 3: single keyword search
    public RoaringBitmap searchSingle(String word) {
        return index.getOrDefault(word, new RoaringBitmap());
    }

    // Task 4 & 5: multiple keyword search
    public RoaringBitmap searchMultiple(String... keywords) {
        if (keywords.length == 0) return new RoaringBitmap();

        RoaringBitmap result = new RoaringBitmap();
        result.or(index.getOrDefault(keywords[0], new RoaringBitmap()));

        for (int i = 1; i < keywords.length; i++) {
            result.and(index.getOrDefault(keywords[i], new RoaringBitmap()));
        }

        return result;
    }

    // Utility to convert bitmap to list of ids
    public List<Integer> toList(RoaringBitmap bitmap) {
        List<Integer> out = new ArrayList<>();
        bitmap.forEach((int x) -> out.add(x));
        return out;
    }

    // Demo
    public static void main(String[] args) {
        BitmapInvertedIndex idx = new BitmapInvertedIndex();

        idx.indexMessage(1, "disk error fatal");
        idx.indexMessage(2, "this is a disk issue");
        idx.indexMessage(3, "fatal disk error occurs");
        idx.indexMessage(4, "warning warning disk");

        RoaringBitmap result = idx.searchMultiple("disk", "error", "fatal");

        System.out.println("Matching docs: " + idx.toList(result));
    }
}
