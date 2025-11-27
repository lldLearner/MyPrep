static final int RETENTION_DAYS = 30;

// mock persistent DB storage
private final List<String> persistentStore = new ArrayList<>();

/** Ingest events + TTL purge */
public void consumeAdEvents(List<AdEvent> events) {

    // TTL purge before processing new events
    LocalDate today = LocalDate.now();
    LocalDate cutoff = today.minusDays(RETENTION_DAYS);
    purgeOldData(cutoff);

    for (AdEvent e : events) {
        seenStore.putIfAbsent(e.adUUID, new HashSet<>());
        Set<SeenEvent> seen = seenStore.get(e.adUUID);

        SeenEvent key = new SeenEvent(e.timestamp, e.type);

        // dedupe
        if (!seen.add(key)) continue;

        // update counters
        eventStore.putIfAbsent(e.adUUID, new TreeMap<>());
        TreeMap<LocalDate, DailyCounter> history = eventStore.get(e.adUUID);

        LocalDate date = e.timestamp.toLocalDate();
        DailyCounter dc = history.getOrDefault(date, new DailyCounter());

        if (e.type == AdEventType.IMPRESSION) dc.impressions++;
        else dc.clicks++;

        history.put(date, dc);
    }
}

/** Purge expired data + dump to persistent storage */
private void purgeOldData(LocalDate cutoffDateExclusive) {

    for (UUID ad : new ArrayList<>(eventStore.keySet())) {

        TreeMap<LocalDate, DailyCounter> history = eventStore.get(ad);
        List<LocalDate> toRemove = new ArrayList<>();

        // iterate from oldest to newest thanks to TreeMap ordering
        for (LocalDate day : history.keySet()) {

            if (day.isBefore(cutoffDateExclusive)) {
                toRemove.add(day);
            } else {
                break; // TreeMap is sorted
            }
        }

        // remove + persist
        for (LocalDate day : toRemove) {
            DailyCounter dc = history.remove(day);
            dumpExpiredDataToPersistentStore(ad, day, dc);
        }

        if (history.isEmpty()) {
            eventStore.remove(ad);
            seenStore.remove(ad);
        }
    }
}

/** Simulated persistence write */
private void dumpExpiredDataToPersistentStore(UUID adId, LocalDate date, DailyCounter dc) {
    String record = "[PERSIST] ad=" + adId + ", date=" + date + ", " + dc;
    persistentStore.add(record);
    System.out.println(record);
}
