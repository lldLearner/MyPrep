/*FOLLOW-UP (c)
Now maintenance happens only after X bookings (Durability), and takes Y minutes (maintenance_time)

Each court tracks:

usageCount
nextAvailableTime
if usageCount == Durability -> add Y maintenance mins and reset usageCount

Approach

For each court store:

(next_free_time, courtId, usageCount)


When booking assigned:

usageCount++

If usageCount == Durability:

nextFreeTime = booking.finish + maintenanceTime

reset usageCount = 0

Else:

nextFreeTime = booking.finish

Use min-heap sorted by nextFreeTime.

Java Code — Follow-up (c)*/
class CourtState {
    int courtId;
    int nextFree;
    int usageCount;

    CourtState(int courtId, int nextFree, int usageCount) {
        this.courtId = courtId;
        this.nextFree = nextFree;
        this.usageCount = usageCount;
    }
}

public static List<CourtAssignment> assignCourtsWithDurability(
        List<BookingRecord> bookings, 
        int maintenanceTime,
        int durability) {

    bookings.sort(Comparator.comparingInt(b -> b.start));
    List<CourtAssignment> result = new ArrayList<>();

    PriorityQueue<CourtState> pq =
            new PriorityQueue<>(Comparator.comparingInt(c -> c.nextFree));

    int courtCounter = 0;

    for (BookingRecord b : bookings) {

        if (!pq.isEmpty() && pq.peek().nextFree <= b.start) {
            CourtState cs = pq.poll();
            cs.usageCount++;

            if (cs.usageCount == durability) {
                cs.nextFree = b.finish + maintenanceTime;
                cs.usageCount = 0;
            } else {
                cs.nextFree = b.finish;
            }

            result.add(new CourtAssignment(b.id, cs.courtId));
            pq.offer(cs);

        } else {
            int newCourt = ++courtCounter;
            CourtState cs = new CourtState(newCourt, b.finish, 1);
            result.add(new CourtAssignment(b.id, newCourt));
            pq.offer(cs);
        }
    }
    return result;
}

Dry run (c)

Durability = 2, maintenance = 3
Bookings: (1–4), (5–7), (8–10), (12–14)

Court1 usage:

(1–4) usage=1

(5–7) usage=2 → needs maintenance
nextFree = 7+3 = 10

(8–10) arrives — NOT free until 10 → new court2.

Complexity

Same O(n log n).
