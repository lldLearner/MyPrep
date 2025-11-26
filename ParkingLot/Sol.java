/*
Core
Multi-level parking lot.
Spot types → Compact, Large, maybe Bike, etc.
Vehicles: Car, Bike, Truck.
park(vehicle) → returns a ParkingTicket if successful.
unpark(ticket) → frees the spot(s).
Must track free spots by type & level.
Must support queries: findVehicle(vehicleNumber).

Vehicle  <-- Car, Bike, Truck, Bus
SpotType <-- COMPACT, LARGE
ParkingSpot
Level
ParkingLot
ParkingTicket
*/

enum VehicleType {
    MOTORCYCLE, CAR, BUS
}
enum SpotType {
    MOTORCYCLE, CAR
}
abstract class Vehicle {
    protected final String plate;
    protected final VehicleType type;
    protected final int spotsNeeded;

    Vehicle(String plate, VehicleType type, int spotsNeeded) {
        this.plate = plate;
        this.type = type;
        this.spotsNeeded = spotsNeeded;
    }

    public String getPlate() { return plate; }
    public int getSpotsNeeded() { return spotsNeeded; }
    public VehicleType getType() { return type; }
}

class Motorcycle extends Vehicle {
    Motorcycle(String p) { super(p, VehicleType.MOTORCYCLE, 1); }
}

class Car extends Vehicle {
    Car(String p) { super(p, VehicleType.CAR, 1); }
}

class Bus extends Vehicle {
    Bus(String p) { super(p, VehicleType.BUS, 5); }
}
class ParkingSpot {
    private final String id;
    private final SpotType type;
    private Vehicle vehicle;

    ParkingSpot(String id, SpotType type) {
        this.id = id;
        this.type = type;
    }

    public boolean isFree() {
        return vehicle == null;
    }

    public boolean canFit(Vehicle v) {
        if (v.getType() == VehicleType.MOTORCYCLE) return true;
        return type == SpotType.CAR;  // Cars and buses require CAR spot
    }

    public void assign(Vehicle v) {
        this.vehicle = v;
    }

    public void free() {
        this.vehicle = null;
    }

    public String getId() { return id; }
    public SpotType getType() { return type; }
}
//The below class is unoptimised park logic
import java.util.concurrent.locks.ReentrantLock;

class Level {
    private final int levelNumber;
    private final List<ParkingSpot> spots;
    private final ReentrantLock lock = new ReentrantLock();

    Level(int levelNumber, List<ParkingSpot> spots) {
        this.levelNumber = levelNumber;
        this.spots = spots;
    }

    public List<ParkingSpot> park(Vehicle v) {
        lock.lock();
        try {
            List<ParkingSpot> allocation = findConsecutiveSpots(v);
            if (allocation == null) return null;

            for (ParkingSpot s : allocation) s.assign(v);
            return allocation;
        } finally {
            lock.unlock();
        }
    }

    private List<ParkingSpot> findConsecutiveSpots(Vehicle v) {
        int need = v.getSpotsNeeded();

        for (int i = 0; i < spots.size(); i++) {
            List<ParkingSpot> candidate = new ArrayList<>();
            int count = 0;

            for (int j = i; j < spots.size() && count < need; j++) {
                ParkingSpot s = spots.get(j);

                if (s.isFree() && s.canFit(v)) {
                    candidate.add(s);
                    count++;
                } else break;
            }

            if (count == need) return candidate;
        }
        return null;
    }

    public void freeSpots(List<ParkingSpot> allocated) {
        lock.lock();
        try {
            for (ParkingSpot s : allocated) s.free();
        } finally {
            lock.unlock();
        }
    }
}
class ParkingTicket {
    final String ticketId;
    final Vehicle vehicle;
    final List<ParkingSpot> spots;
    final long entryTime;

    ParkingTicket(Vehicle v, List<ParkingSpot> spots) {
        this.ticketId = UUID.randomUUID().toString();
        this.vehicle = v;
        this.spots = spots;
        this.entryTime = System.currentTimeMillis();
    }
}
class ParkingLot {
    private final List<Level> levels;
    private final ConcurrentHashMap<String, ParkingTicket> activeTickets = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, ParkingTicket> plateToTicket = new ConcurrentHashMap<>();

    ParkingLot(List<Level> levels) {
        this.levels = levels;
    }

    public ParkingTicket park(Vehicle v) {
        for (Level lvl : levels) {
            List<ParkingSpot> spots = lvl.park(v);
            if (spots != null) {
                ParkingTicket ticket = new ParkingTicket(v, spots);
                activeTickets.put(ticket.ticketId, ticket);
                plateToTicket.put(v.getPlate(), ticket);
                return ticket;
            }
        }
        return null;
    }

    public boolean unpark(String ticketId) {
        ParkingTicket t = activeTickets.remove(ticketId);
        if (t == null) return false;

        plateToTicket.remove(t.vehicle.getPlate());

        // free using Level's concurrency lock
        Level owningLevel = findLevelForSpot(t.spots.get(0));
        owningLevel.freeSpots(t.spots);

        return true;
    }

    private Level findLevelForSpot(ParkingSpot s) {
        String[] parts = s.getId().split("-");
        int levelIdx = Integer.parseInt(parts[0].substring(1)) - 1;
        return levels.get(levelIdx);
    }

    public ParkingTicket findVehicle(String plate) {
        return plateToTicket.get(plate);
    }
}
//main
public class Main {
    public static void main(String[] args) {

        // Create a level with 10 CAR spots
        List<ParkingSpot> spotsLvl1 = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            spotsLvl1.add(new ParkingSpot("L1-" + i, SpotType.CAR));
        }

        Level level1 = new Level(1, spotsLvl1);
        ParkingLot lot = new ParkingLot(List.of(level1));

        // Park a Car
        ParkingTicket t1 = lot.park(new Car("CAR-123"));
        System.out.println("Car 123 parked with ticket: " + t1.ticketId);

        // Park a Bus (needs 5 consecutive)
        ParkingTicket t2 = lot.park(new Bus("BUS-999"));
        System.out.println("Bus parked with ticket: " + t2.ticketId);

        // Find vehicle
        ParkingTicket found = lot.findVehicle("BUS-999");
        System.out.println("Found bus in spots: " +
            found.spots.stream().map(ParkingSpot::getId).toList()
        );

        // Unpark car
        lot.unpark(t1.ticketId);
        System.out.println("Car unparked!");

        // Try parking another bus
        ParkingTicket t3 = lot.park(new Bus("BUS-111"));
        System.out.println("Second bus ticket: " + (t3 == null ? "No space" : t3.ticketId));
    }
}
//optimised lock with ordered map segments

class Level {
    private final int levelNumber;
    private final List<ParkingSpot> spots;
    private final TreeMap<Integer, Integer> freeSegments = new TreeMap<>();
    private final ReentrantLock lock = new ReentrantLock();

    Level(int levelNumber, List<ParkingSpot> spots) {
        this.levelNumber = levelNumber;
        this.spots = spots;

        // Initially all spots free: one single segment
        freeSegments.put(0, spots.size());
    }

    public List<ParkingSpot> park(Vehicle v) {
        lock.lock();
        try {
            int need = v.getSpotsNeeded();
            Integer start = findSegmentOfSize(need);
            if (start == null) return null;

            allocateSegment(start, need);

            List<ParkingSpot> allocation = new ArrayList<>();
            for (int i = 0; i < need; i++) {
                ParkingSpot s = spots.get(start + i);
                s.assign(v);
                allocation.add(s);
            }
            return allocation;

        } finally {
            lock.unlock();
        }
    }

    private Integer findSegmentOfSize(int need) {
        for (var entry : freeSegments.entrySet()) {
            if (entry.getValue() >= need) return entry.getKey();
        }
        return null;
    }

    private void allocateSegment(int start, int need) {
        int currentLen = freeSegments.get(start);
        freeSegments.remove(start);

        int remain = currentLen - need;
        if (remain > 0) {
            freeSegments.put(start + need, remain);
        }
    }

    public void freeSpots(List<ParkingSpot> allocated) {
        lock.lock();
        try {
            int start = getIndex(allocated.get(0));
            int len = allocated.size();

            // mark spots as free
            for (ParkingSpot s : allocated) {
                s.free();
            }

            // merge free segment
            mergeFreeSegment(start, len);

        } finally {
            lock.unlock();
        }
    }

    private void mergeFreeSegment(int start, int len) {
        Integer lower = freeSegments.lowerKey(start);
        Integer higher = freeSegments.higherKey(start);

        int newStart = start;
        int newLen = len;

        // Merge with left segment
        if (lower != null) {
            int lowerLen = freeSegments.get(lower);
            if (lower + lowerLen == start) {
                newStart = lower;
                newLen += lowerLen;
                freeSegments.remove(lower);
            }
        }

        // Merge with right segment
        if (higher != null) {
            int higherLen = freeSegments.get(higher);
            if (start + len == higher) {
                newLen += higherLen;
                freeSegments.remove(higher);
            }
        }

        freeSegments.put(newStart, newLen);
    }

    private int getIndex(ParkingSpot s) {
        return Integer.parseInt(s.getId().split("-")[1]);
    }
}

/*
✔️ 8️⃣ Concurrency Explanation (Uber-Level)
These are the words you MUST say:
🔹 1. Fine-Grained Locking
“I use a ReentrantLock per Level, so concurrency is maximized.
Multiple cars can park on different levels in parallel.
But operations on the same level are serialized to avoid double-booking.”
🔹 2. Thread-safe Maps
“I use ConcurrentHashMap for tickets and plate lookups, ensuring safe concurrent reads/writes.”
🔹 3. Atomic Spot Allocation
“Bus allocation needs 5 consecutive spots. That entire search + assignment happens inside one lock, making it atomic.”
🔹 4. Avoiding Global Lock
“I intentionally avoid locking the entire ParkingLot so we scale linearly with number of levels.”
🔹 5. Production Scale Extension
If asked “How to scale across multiple machines?”:
“I shard by level — each level is owned by a separate microservice.
Then I use Redis Redlock or DB row-locks for distributed spot locking.”
*/


