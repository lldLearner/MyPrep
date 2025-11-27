Q)CustomerRevenueSystem 
 
import java.util.ArrayList; 
import java.util.HashMap; 
import java.util.HashSet; 
import java.util.LinkedList; 
import java.util.List; 
import java.util.Map; 
import java.util.PriorityQueue; 
import java.util.Queue; 
import java.util.Set; 
 
public class CustomerRevenueSystem { 
 
 // customer(referrer) -> customer(referee) adjacency list 
 private Map<Integer, List<Integer>> graph = new HashMap<>(); 
 private Map<Integer, Double> revenueMap = new HashMap<>(); 
 private int customerId = 0; 
 
 // O(1) 
 int insertNewCustomer(double revenue) { 
 
  int id = customerId++; 
  revenueMap.put(id, revenue); 
  graph.put(id, new ArrayList<>()); 
  return id; 
 } 
 
 // O(1) 
 int insertNewCustomer(double revenue, int referrerID) { 
  int id = customerId++; 
  revenueMap.put(id, revenue); 
  graph.put(id, new ArrayList<>()); 
  graph.computeIfAbsent(referrerID, k -> new ArrayList<>()).add(id); 
  return id; 
 } 
 
 // O(referrals) 
 double getTotalRevenueByCustomer(int id) { 
  double revenue = 0; 
  revenue += revenueMap.getOrDefault(id, 0.0); 
  for (int neighbbourId : graph.getOrDefault(id, new ArrayList<>())) { 
   revenue += revenueMap.getOrDefault(neighbbourId, 0.0); 
  } 
 
  return revenue; 
 } 
 
 // O(nlogk) 
 Set<Integer> getLowestKCustomersByMinTotalRevenue(int k, double minTotalRevenue) { 
  PriorityQueue<int[]> topK = new PriorityQueue<>((a, b) -> Double.compare(b[1], a[1])); 
  for (int id : graph.keySet()) { 
   double revenue = getTotalRevenueByCustomer(id); 
   System.out.println(id + " " + revenue); 
   if (revenue >= minTotalRevenue) 
    topK.add(new int[] { id, (int) revenue }); 
   if (topK.size() > k) { 
    topK.poll(); 
   } 
 
  } 
 
  Set<Integer> ans = new HashSet<>(); 
  while (!topK.isEmpty()) { 
   ans.add(topK.poll()[0]); 
  } 
 
  return ans; 
 } 
 
// 1 -> 2 -> 3 
//   -> 4 
O(V + E) 
