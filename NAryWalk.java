🔥 Summary in One Line (for interview)

Perform BFS to find deepest leaves and track the leftmost and rightmost leaf at max depth using a parent pointer + depth map. Then reconstruct both root-to-leaf paths and merge them to produce the final walk.

Q)NaryviewWalk 
package ParkingSpot; 
import java.util.ArrayList; 
import java.util.Collections; 
import java.util.HashMap; 
import java.util.LinkedList; 
import java.util.List; 
import java.util.Map; 
import java.util.Queue; 
public class NaryviewWalk { 
 static class Node { 
  int val; 
  List<Node> children; 
  Node(int v) { 
   this.val = v; 
   this.children = new ArrayList<>(); 
  } 
  @Override 
  public String toString() { 
   // TODO Auto-generated method stub 
   return val + "->"; 
  } 
 } 
 static List<Node> bfs(Node root) { 
  Queue<Node> bfs = new LinkedList<>(); 
  bfs.add(root); 
  Map<Node, Node> parent = new HashMap<>(); 
  Map<Node, Integer> depth = new HashMap<>(); 
  parent.put(root, null); 
  depth.put(root, 0); 
  int maxDepth = 0; 
  Node leftChild = root; 
  Node rightChild = root; 
  while (!bfs.isEmpty()) { 
   Node front = bfs.poll(); 
   System.out.println(front); 
   int frontDepth = depth.get(front); 
   if (front.children.size() == 0) { 
    if (frontDepth > maxDepth) { 
     maxDepth = frontDepth; 
     leftChild = front; 
     rightChild = front; 
    } else if(frontDepth == maxDepth) { 
     rightChild = front; 
    } 
   } 
   for (Node curr : front.children) { 
    bfs.add(curr); 
    parent.put(curr, front); 
    depth.put(curr, frontDepth + 1); 
   } 
  } 
  Node curr = leftChild; 
  List<Node> leftWaalk = new ArrayList<>(); 
  while (curr != null) { 
   leftWaalk.add(curr); 
   curr = parent.get(curr); 
  } 
  curr = rightChild; 
  List<Node> rightWaalk = new ArrayList<>(); 
  while (curr != null) { 
   rightWaalk.add(curr); 
   curr = parent.get(curr); 
  } 
  Collections.reverse(rightWaalk); 
  rightWaalk.remove(0); 
  leftWaalk.addAll(rightWaalk); 
  return leftWaalk; 
 } 
 public static void main(String[] args) { 
  // TODO Auto-generated method stub 
       // EXAMPLE 1: 
       // 
       //         4 
       //       /   \ 
       //      5     7 
       //     /     / \ 
       //    12    1   8 
       // 
       Node root = new Node(4); 
       Node n5 = new Node(5); 
       Node n7 = new Node(7); 
       root.children.add(n5); 
       root.children.add(n7); 
       Node n12 = new Node(12); 
       n5.children.add(n12); 
       Node n1 = new Node(1); 
       Node n8 = new Node(8); 
       n7.children.add(n1); 
       n7.children.add(n8); 
       List<Node> result = bfs(root); 
       System.out.println(result);   // Expected: [12, 5, 4, 7, 8] 
 } 
} 
