 public static void main(String[] args) { 
  LocalDate start = LocalDate.parse("2023-10-01"); 
  LocalDate end = LocalDate.parse("2023-10-03"); 
  int interval = 9; 
  System.out.println("
 ✅
 PART-1 — Generating schedule..."); 
  List<Interval> schedule = createSchedule(start, end, interval); 
  for (Interval iv : schedule) { 
   System.out.println(iv); 
  } 
       System.out.println("\n
 ✅
 PART-2 — Checking timestamps..."); 
       ZonedDateTime t1 = ZonedDateTime.of(2023, 10, 1, 5, 0, 0, 0, ZoneId.of("UTC")); 
       ZonedDateTime t2 = ZonedDateTime.of(2023, 10, 1, 12, 0, 0, 0, ZoneId.of("UTC")); 
       ZonedDateTime t3 = ZonedDateTime.of(2023, 10, 3, 7, 0, 0, 0, ZoneId.of("UTC")); 
       ZonedDateTime t4 = ZonedDateTime.of(2023, 10, 4, 1, 0, 0, 0, ZoneId.of("UTC")); 
       System.out.println("t1 (2023-10-01 05:00Z) → " + isTimestampIncluded(schedule, t1));  
// true 
       System.out.println("t2 (2023-10-01 12:00Z) → " + isTimestampIncluded(schedule, t2));  
// false 
       System.out.println("t3 (2023-10-03 07:00Z) → " + isTimestampIncluded(schedule, t3));  
// true 
       System.out.println("t4 (2023-10-04 01:00Z) → " + isTimestampIncluded(schedule, t4));  
// false 
 } 
} 
 
addInterval O(log N + K) (K = number of merges, usually small) 
 
removeInterval O(log N) 
 
isActive O(log N) 
 
Storage O(N) 
