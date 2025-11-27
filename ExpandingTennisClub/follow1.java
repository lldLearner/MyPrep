/*FOLLOW-UP (a)
Assign bookings to courts (minimum number of courts)

Classic interval scheduling / meeting room allocation.

Approach (interview-style)

Sort booking records by start time.

Use a min-heap (priority queue) where each element is:

(courtFinishTime, courtId)


For each booking:

Check the earliest-finishing court (top of heap).

If its finish time ≤ current booking’s start → reuse that court.

Else → no court is free → create new court.

Assign booking to that court.

Push updated (finishTime, courtId) back into min-heap.

Result = list of (bookingId -> courtId) assignments.

This guarantees minimum number of courts used.

Java Code — Follow-up (a)*/
import java.util.*;

class BookingRecord {
    int id;
    int start;
    int finish;
    BookingRecord(int id, int start, int finish) {
        this.id = id;
        this.start = start;
        this.finish = finish;
    }
}

class CourtAssignment {
    int bookingId;
    int courtId;
    CourtAssignment(int bookingId, int courtId) {
        this.bookingId = bookingId;
        this.courtId = courtId;
    }
}

public class TennisCourtsA {

    public static List<CourtAssignment> assignCourts(List<BookingRecord> bookings) {
        // Sort by start time
        bookings.sort(Comparator.comparingInt(b -> b.start));

        List<CourtAssignment> result = new ArrayList<>();

        // Min-heap of (finish_time, courtId)
        PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt(a -> a[0]));

        int courtCounter = 0;

        for (BookingRecord b : bookings) {
            if (!pq.isEmpty() && pq.peek()[0] <= b.start) {
                // reuse court
                int[] top = pq.poll();
                int courtId = top[1];
                result.add(new CourtAssignment(b.id, courtId));
                pq.offer(new int[]{b.finish, courtId});
            } else {
                // new court
                int courtId = ++courtCounter;
                result.add(new CourtAssignment(b.id, courtId));
                pq.offer(new int[]{b.finish, courtId});
            }
        }

        return result;
    }

    public static void main(String[] args) {
        List<BookingRecord> list = Arrays.asList(
            new BookingRecord(1, 1, 4),
            new BookingRecord(2, 2, 6),
            new BookingRecord(3, 5, 7),
            new BookingRecord(4, 8, 9)
        );
        List<CourtAssignment> ans = assignCourts(list);
        for (CourtAssignment a : ans) {
            System.out.println("Booking " + a.bookingId + " -> Court " + a.courtId);
        }
    }
}

Dry Run (a)

Bookings sorted:
1: (1–4)
2: (2–6)
3: (5–7)
4: (8–9)

Step-by-step:

Booking	Start	Finish	PQ Top	Action
1	1	4	empty	Assign court 1
2	2	6	top: court1 ends @ 4 >2	new court2
3	5	7	top: court1 ends @4 ≤5	reuse court1
4	8	9	top: court1 ends @7 ≤8	reuse court1

Courts used = 2.

Time Complexity

Sort = O(n log n)

Each heap push/pop = O(log n)
Total = O(n log n)
Space = O(n)
