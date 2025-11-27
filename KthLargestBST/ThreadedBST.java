Great — this is a classic follow-up interviewers use after Morris Traversal:

“Since Morris creates temporary threads, can we persist those threads and reuse them so Kth Largest queries are fast?”

This is what they mean by:

“re-use the threaded / linked BST for multiple queries of Kth Largest element.”

Let me explain EXACTLY what they expect from you (clean, interview-style), and then I’ll give code + reasoning.

🚀 Key Insight

Morris Traversal uses temporary threads:

successor.left = root;


Then removes them:

successor.left = null;


But if you KEEP those threads permanently, the tree becomes a:

Threaded Binary Search Tree

Where every node gets a pointer to its inorder predecessor or successor.

This transforms a BST into a structure where:

👉 Next inorder / reverse-inorder successor is O(1)
👉 K-th smallest / K-th largest queries become O(k)

And multiple queries become extremely fast without modifying the tree each time.

🧠 What the interviewer expects you to say
1️⃣ “Yes, I can modify Morris traversal to PERMANENTLY thread the tree.”

That means:

For every node root, find its inorder predecessor or successor

Insert a thread pointer permanently

Do NOT remove the thread like Morris normally does

2️⃣ Then I can answer any number of queries like:

K = 1 (largest)

K = 10

K = 100

multiple times

all in O(K) time (instead of O(N) per query)

3️⃣ And I don’t need stack or recursion — tree is threaded.
⚡ Two Types of Threaded Trees

Depending on the direction:

A. Right-threaded BST

Each node holds a pointer to its inorder successor
→ Makes K-th smallest fast

B. Left-threaded BST

Each node holds a pointer to its reverse-inorder successor
→ Makes K-th largest fast

Since your question is K-th Largest, we want Left-threaded BST.

🔥 Constructing a Left-threaded BST (using reverse Morris)
Algorithm:

Perform reverse Morris traversal (Right → Root → Left)

For each node:

If it has NO right child, set that right to temporary thread (its next larger node)

But this time: KEEP the thread permanently

After threading, every node has:

right: actual child OR thread to next larger node

left: actual child

Then for K-th largest:

Start at largest node (rightmost)

Follow right pointers K–1 times

💡 Complexity
Operation	Time	Space
Thread creation	O(N)	O(1)
Each Kth-largest query	O(K)	O(1)
Multiple queries (M times)	O(N + M*K)	O(1)

Compared to Morris each time: O(M*N)

Threaded BST is obviously superior for multiple queries.

package ParkingSpot;

public class ThreadedKthLargestBST {

    static class Node {
        int val;
        Node left, right;
        boolean isThread; // true if right pointer is a thread instead of actual child

        Node(int val) { this.val = val; }
    }

    // --------------------------------------------------
    // BUILD LEFT-THREADED BST (using reverse Morris)
    // --------------------------------------------------
    public void createLeftThreadedBST(Node root) {
        Node current = root;
        Node prev = null; // previous visited node in reverse inorder

        while (current != null) {

            if (current.right == null) {
                // Create a thread
                current.right = prev;
                current.isThread = true;

                prev = current;
                current = current.left;
            }
            else {
                // Find successor (left-most of right subtree)
                Node succ = current.right;

                while (succ.left != null && succ.left != current) {
                    succ = succ.left;
                }

                if (succ.left == null) {
                    // Create temporary link
                    succ.left = current;
                    current = current.right;
                }
                else {
                    // Remove temporary link
                    succ.left = null;

                    // Now process current
                    current.right = prev;
                    current.isThread = true;

                    prev = current;
                    current = current.left;
                }
            }
        }
    }

    // --------------------------------------------------
    // K-th Largest using the threaded BST
    // --------------------------------------------------
    public int kthLargest(Node root, int k) {
        if (root == null || k <= 0) return -1;

        // 1. Move to the largest node (rightmost non-threaded)
        Node curr = root;
        while (curr.right != null && !curr.isThread) {
            curr = curr.right;
        }

        // curr is now the largest node
        // 2. Follow threads k-1 times
        while (curr != null && k > 1) {
            curr = curr.right; // follow the thread
            k--;
        }

        return curr != null ? curr.val : -1;
    }

    // --------------------------------------------------
    // MAIN FOR TESTING
    // --------------------------------------------------
    public static void main(String[] args) {

        Node root = new Node(10);
        root.left = new Node(4);
        root.left.left = new Node(2);
        root.right = new Node(15);
        root.right.left = new Node(12);
        root.right.right = new Node(20);
        root.right.right.right = new Node(40);

        ThreadedKthLargestBST sol = new ThreadedKthLargestBST();

        // Step 1: Create threaded BST (done once)
        sol.createLeftThreadedBST(root);

        // Step 2: Run multiple queries efficiently
        System.out.println(sol.kthLargest(root, 1)); // 40
        System.out.println(sol.kthLargest(root, 2)); // 20
        System.out.println(sol.kthLargest(root, 3)); // 15
        System.out.println(sol.kthLargest(root, 4)); // 12
        System.out.println(sol.kthLargest(root, 5)); // 10
        System.out.println(sol.kthLargest(root, 6)); // 4
        System.out.println(sol.kthLargest(root, 7)); // 2
    }
}
