/*FOLLOW-UP (b)
Now add fixed maintenance time X after each booking

Meaning court becomes free at:

freeTime = booking.finish + maintenanceTime

Approach

Everything same as (a) except:

When pushing back court to heap, push:

(finish + X, courtId)

Java Code — Follow-up (b)*/
public static List<CourtAssignment> assignCourtsWithMaintenance(
        List<BookingRecord> bookings, int maintenanceTime) {

    bookings.sort(Comparator.comparingInt(b -> b.start));
    List<CourtAssignment> result = new ArrayList<>();
    PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt(a -> a[0]));
    int courtCounter = 0;

    for (BookingRecord b : bookings) {
        if (!pq.isEmpty() && pq.peek()[0] <= b.start) {
            int[] top = pq.poll();
            int courtId = top[1];
            result.add(new CourtAssignment(b.id, courtId));
            pq.offer(new int[]{b.finish + maintenanceTime, courtId});
        } else {
            int courtId = ++courtCounter;
            result.add(new CourtAssignment(b.id, courtId));
            pq.offer(new int[]{b.finish + maintenanceTime, courtId});
        }
    }

    return result;
}

Dry Run (b)

Take X = 2:

Booking 1: finish=4 → free at 4+2=6
Booking 2: start=2 — can't use court1 since free at 6 → new court2
Booking 3: start=5 — court1 free at 6 >5 → new court3
Booking 4: start=8 — many become free.

More courts used.
