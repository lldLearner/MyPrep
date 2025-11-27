FOLLOW-UP (d)
Find minimum number of courts required (no need to assign courts)

This is the meeting rooms problem.

Approach

Create two arrays:

starts[]

ends[]

Sort both.

Sweep:

if starts[i] < ends[j] → need a new court

else → reuse a court (j++)

Track maximum overlap.

Java Code — Follow-up (d)
public static int minCourtsNeeded(List<BookingRecord> bookings) {
    int n = bookings.size();
    int[] starts = new int[n];
    int[] ends = new int[n];

    for (int i = 0; i < n; i++) {
        starts[i] = bookings.get(i).start;
        ends[i] = bookings.get(i).finish;
    }

    Arrays.sort(starts);
    Arrays.sort(ends);

    int i = 0, j = 0;
    int used = 0, maxUsed = 0;

    while (i < n) {
        if (starts[i] < ends[j]) {
            used++;
            maxUsed = Math.max(maxUsed, used);
            i++;
        } else {
            used--;
            j++;
        }
    }

    return maxUsed;
}

Dry run (d)

Starts: 1,2,5,8
Ends: 4,6,7,9

Sweeping → max overlap = 2 courts.

FOLLOW-UP (e)
Check if two bookings conflict

Two intervals (s1, e1) and (s2, e2) conflict if:

NOT (e1 <= s2 OR e2 <= s1)

Java Code — Follow-up (e)
public static boolean isConflict(BookingRecord a, BookingRecord b) {
    return !(a.finish <= b.start || b.finish <= a.start);
}

Dry run (e)

Booking A: 1–4
Booking B: 4–6 → NO conflict (finish == start = okay)
Booking C: 3–5 → conflict with A.
