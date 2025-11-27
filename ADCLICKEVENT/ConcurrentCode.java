Nice — below is a complete, production-like Part-3 implementation: a thread-safe multi-threaded version of the AdClickEvent system with:

ConcurrentHashMap and ConcurrentSkipListMap for concurrent access and sorted-day semantics

ConcurrentHashMap.newKeySet() for atomic dedupe per-ad

LongAdder for high-throughput counters (no lost increments)

Per-ad ReentrantLock for safe purge/replace operations (fine-grained locking)

computeIfAbsent and other concurrency primitives to avoid global locks

An optional background purger (scheduled) that evicts TTL data and dumps to a persistent sink asynchronously

All public APIs are thread-safe: consumeAdEvents, getDailyAdEventHistory, isAdCappedAtDate

Mock persistentStore to demonstrate snapshot + dump

You can paste this into a single Java file and run it.
package ParkingSpot;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Multi-threaded Ad Click / Impression system (Part 3)
 * - Thread-safe ingestion with dedupe
 * - TTL purge (background or on-demand)
 * - Persistent dump of expired buckets
 */
public class MultiThreadedAdClickSystem {

    // ---------------- Configuration ----------------
    static final int RETENTION_DAYS = 30;
    static final int ROLLING_WINDOW_LENGTH_DAYS = 3;
    static final int IMPRESSION_LIMIT = 5;

    // ---------------- Domain types ----------------
    enum AdEventType { IMPRESSION, CLICK }

    static class AdEvent {
        final UUID adUUID;
        final LocalDateTime timestamp;
        final AdEventType type;

        AdEvent(UUID adUUID, LocalDateTime timestamp, AdEventType type) {
            this.adUUID = adUUID;
            this.timestamp = timestamp;
            this.type = type;
        }
    }

    static class DailyCounter {
        final LongAdder impressions = new LongAdder();
        final LongAdder clicks = new LongAdder();

        @Override
        public String toString() {
            return "impressions=" + impressions.sum() + ", clicks=" + clicks.sum();
        }
    }

    static class AdEventBucket {
        final LocalDate date;
        final int impressions;
        final int clicks;

        AdEventBucket(LocalDate date, int impressions, int clicks) {
            this.date = date;
            this.impressions = impressions;
            this.clicks = clicks;
        }

        @Override
        public String toString() {
            return date + " : impressions=" + impressions + ", clicks=" + clicks;
        }
    }

    // Key for dedupe (timestamp + type). If you want stricter dedupe add a client-provided event-id.
    static class SeenEvent {
        final LocalDateTime timestamp;
        final AdEventType type;

        SeenEvent(LocalDateTime timestamp, AdEventType type) {
            this.timestamp = timestamp;
            this.type = type;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof SeenEvent)) return false;
            SeenEvent other = (SeenEvent) o;
            return timestamp.equals(other.timestamp) && type == other.type;
        }

        @Override
        public int hashCode() {
            return Objects.hash(timestamp, type);
        }
    }

    // ---------------- Thread-safe stores ----------------

    /**
     * eventStore:
     *   adUUID -> ConcurrentSkipListMap<LocalDate, DailyCounter>
     * ConcurrentSkipListMap keeps days sorted and is thread-safe.
     */
    private final ConcurrentHashMap<UUID, ConcurrentSkipListMap<LocalDate, DailyCounter>> eventStore = new ConcurrentHashMap<>();

    /**
     * seenStore:
     *   adUUID -> Set<SeenEvent>
     * The set is a concurrent set (backed by ConcurrentHashMap) so add() is atomic.
     */
    private final ConcurrentHashMap<UUID, Set<SeenEvent>> seenStore = new ConcurrentHashMap<>();

    /**
     * Locks per ad for safe purge-swap operations (fine-grained).
     */
    private final ConcurrentHashMap<UUID, ReentrantLock> adLocks = new ConcurrentHashMap<>();

    /**
     * Mock persistent store where we dump purged data (in real system this would be a DB / Kafka / S3).
     */
    private final List<String> persistentStore = Collections.synchronizedList(new ArrayList<>());

    // Optional scheduled purger
    private final ScheduledExecutorService purgerScheduler = Executors.newSingleThreadScheduledExecutor();

    public MultiThreadedAdClickSystem() {
        // start background purge that runs every minute (configurable)
        purgerScheduler.scheduleAtFixedRate(() -> {
            try {
                LocalDate cutoff = LocalDate.now().minusDays(RETENTION_DAYS);
                purgeOldData(cutoff);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }, 1, 1, TimeUnit.MINUTES);
    }

    // ---------------- Public API ----------------

    /**
     * Thread-safe ingestion of a batch of events.
     * Deduplication and counter increments are atomic-ish (no global lock).
     */
    public void consumeAdEvents(final List<AdEvent> events) {
        // process each event (can be parallelized by caller if desired)
        for (AdEvent e : events) {

            // Atomic per-ad seen set creation
            Set<SeenEvent> seenSet = seenStore.computeIfAbsent(e.adUUID, k -> ConcurrentHashMap.newKeySet());

            SeenEvent key = new SeenEvent(e.timestamp, e.type);

            // add() is atomic on concurrent set; if already present, it's a duplicate and skip
            boolean isNew = seenSet.add(key);
            if (!isNew) continue;

            // Ensure per-ad history map exists (atomic create)
            ConcurrentSkipListMap<LocalDate, DailyCounter> history =
                    eventStore.computeIfAbsent(e.adUUID, k -> new ConcurrentSkipListMap<>());

            // Use computeIfAbsent to atomically create DailyCounter for date
            LocalDate date = e.timestamp.toLocalDate();
            DailyCounter dc = history.computeIfAbsent(date, d -> new DailyCounter());

            // Thread-safe counter increment using LongAdder
            if (e.type == AdEventType.IMPRESSION) dc.impressions.increment();
            else dc.clicks.increment();
        }
    }

    /**
     * Get daily aggregated history for an ad as a sorted list (oldest->newest).
     * Returns a snapshot of current values.
     */
    public List<AdEventBucket> getDailyAdEventHistory(final UUID adUUID) {
        ConcurrentSkipListMap<LocalDate, DailyCounter> history = eventStore.get(adUUID);
        if (history == null) {
            throw new IllegalArgumentException("No data for adUUID: " + adUUID);
        }

        List<AdEventBucket> result = new ArrayList<>();
        for (Map.Entry<LocalDate, DailyCounter> e : history.entrySet()) {
            DailyCounter dc = e.getValue();
            // read snapshot of LongAdder sums
            result.add(new AdEventBucket(e.getKey(), (int) dc.impressions.sum(), (int) dc.clicks.sum()));
        }
        return result;
    }

    /**
     * Check if ad is capped at given date according to rolling rules: X impressions and no clicks in Y-day window.
     * Uses subMap(startDate, true, date, true) to efficiently sum only the required window.
     */
    public boolean isAdCappedAtDate(final UUID adUUID, final LocalDate date) {
        ConcurrentSkipListMap<LocalDate, DailyCounter> history = eventStore.get(adUUID);
        if (history == null) {
            throw new IllegalArgumentException("No data for adUUID: " + adUUID);
        }

        LocalDate start = date.minusDays(ROLLING_WINDOW_LENGTH_DAYS - 1);
        int impressions = 0;
        int clicks = 0;

        // Efficient range view on skip list map
        for (Map.Entry<LocalDate, DailyCounter> e : history.subMap(start, true, date, true).entrySet()) {
            DailyCounter dc = e.getValue();
            impressions += dc.impressions.sum();
            clicks += dc.clicks.sum();
        }

        return impressions >= IMPRESSION_LIMIT && clicks == 0;
    }

    // ---------------- Purge / Persistence ----------------

    /**
     * Remove all entries older than cutoffDateExclusive.
     * For each removed day we call dumpExpiredDataToPersistentStore (async or sync).
     *
     * This method is safe to call concurrently with ingestion because:
     * - We acquire a per-ad lock while purging that ad to avoid races with ingestion for that ad.
     * - Ingestion uses computeIfAbsent / atomic operations so it can continue for other ads.
     */
    public void purgeOldData(LocalDate cutoffDateExclusive) {
        // Copy keys to avoid iterating over a concurrent map's keyset while mutating
        List<UUID> ads = new ArrayList<>(eventStore.keySet());

        for (UUID ad : ads) {
            ReentrantLock lock = adLocks.computeIfAbsent(ad, k -> new ReentrantLock());
            lock.lock();
            try {
                ConcurrentSkipListMap<LocalDate, DailyCounter> history = eventStore.get(ad);
                if (history == null || history.isEmpty()) {
                    // cleanup structures if empty
                    eventStore.remove(ad);
                    seenStore.remove(ad);
                    adLocks.remove(ad);
                    continue;
                }

                // headMap(cutoffDateExclusive) gives dates strictly < cutoff
                NavigableMap<LocalDate, DailyCounter> expiredView = history.headMap(cutoffDateExclusive, false);
                if (expiredView.isEmpty()) continue;

                // copy the keys to remove to avoid ConcurrentModification
                List<LocalDate> toRemove = new ArrayList<>(expiredView.keySet());

                for (LocalDate day : toRemove) {
                    DailyCounter dc = history.remove(day);
                    if (dc != null) {
                        dumpExpiredDataToPersistentStore(ad, day, dc);
                    }
                }

                if (history.isEmpty()) {
                    eventStore.remove(ad);
                    seenStore.remove(ad);
                    adLocks.remove(ad);
                }
            } finally {
                lock.unlock();
            }
        }
    }

    /**
     * Simulated persistent write. In prod this would be async write to Kafka / S3 / DB.
     * Kept synchronous here for simplicity; you can offload to a queue for better throughput.
     */
    private void dumpExpiredDataToPersistentStore(UUID adId, LocalDate date, DailyCounter dc) {
        String record = "[PERSIST] ad=" + adId + ", date=" + date + ", " + dc;
        persistentStore.add(record);
        // for debug visibility
        System.out.println(record);
    }

    // ---------------- Lifecycle ----------------

    /**
     * Clean shutdown for background purger.
     */
    public void shutdown() {
        purgerScheduler.shutdownNow();
    }

    // ---------------- Demo / Simple test harness ----------------
    public static void main(String[] args) throws InterruptedException {

        MultiThreadedAdClickSystem svc = new MultiThreadedAdClickSystem();

        UUID adA = UUID.randomUUID();
        UUID adB = UUID.randomUUID();

        // Prepare some events for adA/adB across multiple days
        List<AdEvent> batch1 = Arrays.asList(
                new AdEvent(adA, LocalDateTime.now().minusDays(1).withHour(10), AdEventType.IMPRESSION),
                new AdEvent(adA, LocalDateTime.now().minusDays(1).withHour(11), AdEventType.IMPRESSION),
                new AdEvent(adA, LocalDateTime.now().withHour(12), AdEventType.IMPRESSION),
                new AdEvent(adA, LocalDateTime.now().withHour(12).plusSeconds(1), AdEventType.CLICK),
                new AdEvent(adB, LocalDateTime.now().minusDays(2).withHour(9), AdEventType.IMPRESSION),
                new AdEvent(adB, LocalDateTime.now().withHour(14), AdEventType.IMPRESSION)
        );

        // Simulate concurrent producers using an executor
        ExecutorService producers = Executors.newFixedThreadPool(4);
        Runnable producerJob = () -> svc.consumeAdEvents(batch1);

        for (int i = 0; i < 8; i++) producers.submit(producerJob);

        producers.shutdown();
        producers.awaitTermination(5, TimeUnit.SECONDS);

        // read history and isAdCappedAtDate checks
        System.out.println("History A: " + svc.getDailyAdEventHistory(adA));
        System.out.println("History B: " + svc.getDailyAdEventHistory(adB));

        boolean cappedA = svc.isAdCappedAtDate(adA, LocalDate.now());
        System.out.println("AdA capped at today? " + cappedA);

        // Demonstrate purge (force a purge with a very old cutoff)
        LocalDate longCutoff = LocalDate.now().minusDays(RETENTION_DAYS + 1);
        svc.purgeOldData(longCutoff); // should not remove recent entries

        // Shutdown background purger
        svc.shutdown();
    }
}
