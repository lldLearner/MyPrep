package ParkingSpot;

public class KthLargestElementInBSTMorris {

    static class Node {
        int val;
        Node left, right;

        Node(int val) {
            this.val = val;
        }
    }

    public Integer findKthLargest(Node root, int k) {
        int count = 0;

        while (root != null) {

            // No right child → visit node
            if (root.right == null) {
                count++;
                if (count == k) return root.val;
                root = root.left;
            } 
            else {
                // Find inorder successor in reverse inorder
                Node succ = root.right;

                while (succ.left != null && succ.left != root) {
                    succ = succ.left;
                }

                // Create thread
                if (succ.left == null) {
                    succ.left = root;
                    root = root.right;
                }
                // Remove thread (successor found)
                else {
                    succ.left = null;
                    count++;
                    if (count == k) return root.val;
                    root = root.left;
                }
            }
        }

        return null; // k too large
    }

    public static void main(String[] args) {

        Node root = new Node(10);
        root.left = new Node(4);
        root.left.left = new Node(2);
        root.right = new Node(15);
        root.right.left = new Node(12);
        root.right.right = new Node(20);
        root.right.right.right = new Node(40);

        Integer ans = new KthLargestElementInBSTMorris().findKthLargest(root, 3);
        System.out.println(ans); // Output: 15
    }
}
