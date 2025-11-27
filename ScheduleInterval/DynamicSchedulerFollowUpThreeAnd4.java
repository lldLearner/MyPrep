// [CHANGE - Follow-up 3 & 4: Dynamic scheduler with TreeMap + merge 
   static class DynamicScheduler { 
 
    TreeMap<ZonedDateTime, Interval> map = new TreeMap<>(); 
 
    // ---------------------------- 
    // ADD INTERVAL WITH FULL MERGING 
    // ---------------------------- 
 
    void addInterval(Interval newIv) { 
 
        // Merge with all overlapping or touching intervals 
        Map.Entry<ZonedDateTime, Interval> entry = map.floorEntry(newIv.st); 
 
        // Check prev interval for overlap or abutment 
        if (entry != null && !entry.getValue().en.isBefore(newIv.st)) { 
            newIv = new Interval(entry.getValue().st,  
                                 max(entry.getValue().en, newIv.en)); 
            map.remove(entry.getKey()); 
        } 
 
        // Merge with all following intervals that overlap/abut 
        entry = map.ceilingEntry(newIv.st); 
        while (entry != null && !entry.getValue().st.isAfter(newIv.en)) { 
 
            newIv = new Interval(newIv.st,  
                                 max(newIv.en, entry.getValue().en)); 
 
            map.remove(entry.getKey()); 
            entry = map.ceilingEntry(newIv.st); 
        } 
 
        // Insert merged interval 
        map.put(newIv.st, newIv); 
    } 
 
 
    // ---------------------------- 
    // REMOVE INTERVAL WITH SPLITTING 
    // ---------------------------- 
    void removeInterval(Interval iv) { 
 
        Map.Entry<ZonedDateTime, Interval> entry = map.floorEntry(iv.st); 
        if (entry == null) return; 
 
        Interval curr = entry.getValue(); 
        if (curr.en.isBefore(iv.st) || curr.st.isAfter(iv.en)) 
            return; // no overlap 
 
        map.remove(entry.getKey()); 
 
        // If left piece remains after removal 
        if (curr.st.isBefore(iv.st)) { 
            map.put(curr.st, new Interval(curr.st, iv.st)); 
        } 
 
        // If right piece remains after removal 
        if (curr.en.isAfter(iv.en)) { 
            map.put(iv.en, new Interval(iv.en, curr.en)); 
        } 
    } 
 
 
    // ---------------------------- 
    // QUERY ACTIVE TIMESTAMP 
    // ---------------------------- 
    boolean isActive(ZonedDateTime ts) { 
        Map.Entry<ZonedDateTime, Interval> e = map.floorEntry(ts); 
        if (e == null) return false; 
        Interval iv = e.getValue(); 
        return !ts.isBefore(iv.st) && !ts.isAfter(iv.en); 
    } 
 
    private ZonedDateTime max(ZonedDateTime a, ZonedDateTime b) { 
        return a.isAfter(b) ? a : b; 
    }} 
