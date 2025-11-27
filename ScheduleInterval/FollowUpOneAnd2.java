Q)Schedule Interval 
Part1 - createSchedule(LocalDate st, LocalDate end, int hours) 
Part2 - isTimestampIncluded(List<Interval> intervals, ZonedDateTime timestamp)  
 
package ParkingSpot; 
import java.time.*; 
import java.util.*; 
public class IntervalScheduler { 
   // [CHANGE - Follow-up 1: Immutable interval] 
   static class Interval { 
       final ZonedDateTime st; 
       final ZonedDateTime en; 
       public Interval(ZonedDateTime st, ZonedDateTime en) { 
 
           this.st = st; 
           this.en = en; 
       } 
       @Override 
       public String toString() { 
           return st + " → " + en; 
       } 
   } 
   // [CHANGE - Follow-up 1: Utility function] 
   static ZonedDateTime toUTCStart(LocalDate d) { 
       return d.atStartOfDay(ZoneId.of("UTC")); 
   } 
   static List<Interval> createSchedule(LocalDate st, LocalDate end, int hours) { 
       // [CHANGE - Follow-up 1: Input validation] 
       if (hours <= 0) { 
           throw new IllegalArgumentException("Interval hours must be > 0"); 
       } 
       if (end.isBefore(st)) { 
           throw new IllegalArgumentException("End date must not be before start date."); 
       } 
       List<Interval> ans = new ArrayList<>(); 
       // [CHANGE - Follow-up 1: using utility] 
       ZonedDateTime stD = toUTCStart(st); 
       ZonedDateTime enD = toUTCStart(end).plusDays(1).minusNanos(1); 
       boolean isOn = true; 
       while (stD.isBefore(enD)) { 
           ZonedDateTime next = stD.plusHours(hours); 
           if (isOn) { 
               ZonedDateTime endInterval = next.isBefore(enD) ? next : enD; 
               ans.add(new Interval(stD, endInterval)); 
           } 
           isOn = !isOn; 
           stD = next; 
       } 
       return ans; 
   } 
   // Linear check 
   // [CHANGE - Follow-up 2: added linear scan] 
   static boolean isTimestampIncludedLinear(List<Interval> intervals, ZonedDateTime ts) { 
       for (Interval iv : intervals) { 
           if (!ts.isBefore(iv.st) && !ts.isAfter(iv.en)) 
               return true; 
       } 
       return false; 
   } 
   // Binary search check 
   static boolean isTimestampIncluded(List<Interval> intervals, ZonedDateTime ts) { 
       int l = 0, r = intervals.size() - 1; 
       while (l <= r) { 
           int mid = (l + r) >>> 1; 
           Interval iv = intervals.get(mid); 
           if (!ts.isBefore(iv.st) && !ts.isAfter(iv.en)) { 
               return true; 
           } 
           if (ts.isBefore(iv.st)) r = mid - 1; 
           else l = mid + 1; 
       } 
       return false; 
   } 
