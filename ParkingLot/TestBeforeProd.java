/*
✅ FOLLOW-UP 2: How will you test your solution before deploying to production?

You need to cover:

Unit tests

Integration tests

Load tests

Chaos/error tests

Concurrency tests

Here is the version you should say in interview:

⭐ (A) Unit Tests — Most Important

“I will test each class independently: ParkingSpot, Level, ParkingLot, Vehicle, and Ticket.”

Examples:

ParkingSpot

canFit() for all combinations

isFree(), assign(), free()

Level

Correct parking of motorcycle, car

Bus gets 5 consecutive car spots

Fails when not enough consecutive spots

Frees all spots correctly

ParkingLot

parkVehicle() returns ticket

findVehicle() returns correct ticket

unpark() frees spots

Multiple levels parking logic

⭐ (B) Integration Tests

“Test end-to-end scenarios.”

Examples:

Fill entire parking lot → next park returns null

Park multiple vehicles → ensure correct spot allocation

Unpark and repark → ensure reused spots

Bus cannot park until 5 consecutive spots open up

⭐ (C) Concurrency Tests

“Use multi-threaded tests with 50–100 threads trying to park and unpark.”

Test:

Two cars trying to take the same spot simultaneously

Bus waiting for 5 consecutive spots while cars fill gaps

No deadlocks

No double-assignment to a spot

⭐ (D) Load Tests

“Simulate 10,000 park/unpark ops per second.”

Measure:

Latency of park()

Contention on synchronized/locks

Memory usage of tickets/spots

⭐ (E) Chaos/Error Tests

“I simulate failures: null tickets, wrong ticket ID, invalid vehicle, disk failure in datastore version.”

Edge cases:

double unpark

missing ticket

corrupted ticket

vehicle parked that doesn’t exist in tracking map
*/
