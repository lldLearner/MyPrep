Q)AdClickEventSystem 
Part 1 -> Single Threaded + Dedupe 
public class AdClickSystem { 
   // Configurable rolling window / cap 
   static final int ROLLING_WINDOW_LENGTH_DAYS = 3; 
   static final int IMPRESSION_LIMIT = 5; 
   // Main stores (single-threaded; no concurrency control) 
   // eventStore: adUUID -> (date -> DailyCounter) 
   private final Map<UUID, TreeMap<LocalDate, DailyCounter>> eventStore = new HashMap<>(); 
   // seenStore: adUUID -> set of seen events for deduplication (by timestamp + type) 
   private final Map<UUID, Set<SeenEvent>> seenStore = new HashMap<>(); 
   // -------------------- Domain types -------------------- 
   enum AdEventType { 
       IMPRESSION, CLICK 
   } 
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
       int impressions = 0; 
       int clicks = 0; 
       @Override 
       public String toString() { 
           return "impressions=" + impressions + ", clicks=" + clicks; 
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
   // helper key for dedupe 
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
   // -------------------- Public API -------------------- 
   /** 
    * Consume a batch of ad events. 
    * Deduplicate per (timestamp + type) for the same ad UUID. 
    * Update daily counters. 
    */ 
   public void consumeAdEvents(final List<AdEvent> events) { 
       for (AdEvent e : events) { 
           seenStore.putIfAbsent(e.adUUID, new HashSet<>()); 
           Set<SeenEvent> seen = seenStore.get(e.adUUID); 
           SeenEvent key = new SeenEvent(e.timestamp, e.type); 
           if (seen.contains(key)) { 
               // duplicate - skip 
               // (In production we might log/debug counters instead) 
               continue; 
           } 
           // mark seen 
           seen.add(key); 
           // update daily counter 
           eventStore.putIfAbsent(e.adUUID, new TreeMap<>()); 
           TreeMap<LocalDate, DailyCounter> history = eventStore.get(e.adUUID); 
           LocalDate date = e.timestamp.toLocalDate(); 
           DailyCounter dc = history.getOrDefault(date, new DailyCounter()); 
           if (e.type == AdEventType.IMPRESSION) { 
               dc.impressions++; 
           } else { 
               dc.clicks++; 
           } 
           history.put(date, dc); 
       } 
   } 
   public List<AdEventBucket> getDailyAdEventHistory(final UUID adUUID) { 
       TreeMap<LocalDate, DailyCounter> history = eventStore.get(adUUID); 
       if (history == null) { 
           throw new IllegalArgumentException("No data for adUUID: " + adUUID); 
       } 
       List<AdEventBucket> result = new ArrayList<>(); 
       for (Map.Entry<LocalDate, DailyCounter> e : history.entrySet()) { 
           result.add(new AdEventBucket(e.getKey(), e.getValue().impressions, 
e.getValue().clicks)); 
       } 
       return result; 
   } 
   public boolean isAdCappedAtDate(final UUID adUUID, final LocalDate date) { 
       TreeMap<LocalDate, DailyCounter> history = eventStore.get(adUUID); 
       if (history == null) { 
           throw new IllegalArgumentException("No data for adUUID: " + adUUID); 
       } 
       LocalDate start = date.minusDays(ROLLING_WINDOW_LENGTH_DAYS - 1); 
       int impressions = 0; 
       int clicks = 0; 
 
       for (LocalDate d = start; !d.isAfter(date); d = d.plusDays(1)) { 
           DailyCounter dc = history.getOrDefault(d, null); 
           if (dc != null) { 
               impressions += dc.impressions; 
               clicks += dc.clicks; 
           } 
       } 
       return impressions >= IMPRESSION_LIMIT && clicks == 0; 
   } 
} 
