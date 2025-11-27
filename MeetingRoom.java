Q)MeetingRoom 
package ParkingSpot; 
import java.util.ArrayDeque; 
import java.util.ArrayList; 
import java.util.Deque; 
import java.util.Iterator; 
import java.util.LinkedList; 
import java.util.List; 
import java.util.PriorityQueue; 
import java.util.Queue; 
import java.util.UUID; 
public class MeetingScheduler { 
 static class RoomSlot { 
  int end; 
  String room; 
  public RoomSlot(int end, String room) { 
   super(); 
   this.end = end; 
   this.room = room; 
  } 
 } 
 private final PriorityQueue<RoomSlot> busy = new PriorityQueue<>((a, b) -> 
Integer.compare(a.end, b.end)); 
 private final Queue<String> availableRooms = new LinkedList<>(); 
 private final Deque<Reservation> lastNScheduled = new ArrayDeque<>(); 
 private final int MAX_LOG_SIZE = 5; 
 public MeetingScheduler(List<String> availableRooms) { 
  // TODO Auto-generated constructor stub 
  this.availableRooms.addAll(new ArrayList<>(availableRooms)); 
 } 
 class Reservation { 
  UUID id; 
  String room; 
  int start; 
  int end; 
  public Reservation(UUID id, String room, int start, int end) { 
   super(); 
   this.id = id; 
   this.room = room; 
   this.start = start; 
   this.end = end; 
  } 
  public String toString() { 
   return "{id=" + id + ", room=" + room + ", start=" + start + ", end=" 
+ end + "}"; 
  } 
 } 
 public Reservation reserveRoom(int start, int end) { 
  while (!busy.isEmpty() && start >= busy.peek().end) { 
   availableRooms.add(busy.poll().room); 
  } 
  if (availableRooms.isEmpty()) { 
   throw new IllegalStateException("No available room found!"); 
  } 
  String room = availableRooms.poll(); 
  busy.offer(new RoomSlot(end, room)); 
  Reservation res = new Reservation(UUID.randomUUID(), room, start, end); 
  lastNScheduled.add(res); 
  if (lastNScheduled.size() > MAX_LOG_SIZE) { 
   lastNScheduled.removeFirst(); 
  } 
  return res; 
 } 
 public void cleanUpBusyRooms(int currentTime) { 
  while (!busy.isEmpty() && busy.peek().end < currentTime) { 
   busy.poll(); 
  } 
 } 
 private final int RETENTION_PERIOD = 2; 
 public void purgeLogQueue(int currentTime) { 
  while (!lastNScheduled.isEmpty()) { 
   Reservation res = lastNScheduled.peekFirst(); 
   if (res.end < currentTime - RETENTION_PERIOD) { 
    lastNScheduled.pollFirst(); 
   } else { 
    break; 
   } 
  } 
 } 
 public List<Reservation> lastNMeeting(int n) { 
  Iterator<Reservation> it = lastNScheduled.descendingIterator(); 
  int count = 0; 
  List<Reservation> ans = new ArrayList<>(); 
  while (it.hasNext() && count < n) { 
   ans.add(it.next()); 
   count++; 
  } 
  return ans; 
 } 
  
} 
 
Method Time Complexity Space Complexity Explanation 
reserveRoom() O(log R) O(1) PQ insertion + amortized 
polling 
cleanUpBusyRooms() O(R log R) worst; O(log R) 
amortized 
O(1) Removes expired meetings 
purgeLogQueue() O(1) O(1) Bounded to 5 entries 
lastNMeeting(n) O(1) O(1) n ≤ 5 due to log size 
Constructor O(R) O(R) Copies room list 
