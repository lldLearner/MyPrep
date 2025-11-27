// QuadTree Node 
class QuadNode { 
    int val;                // value of the region (only meaningful if leaf) 
    boolean isLeaf;         // true if this node is uniform 
    QuadNode topLeft; 
    QuadNode topRight; 
    QuadNode bottomLeft; 
    QuadNode bottomRight; 
 
    // Leaf node 
    public QuadNode(int val, boolean isLeaf) { 
        this.val = val; 
        this.isLeaf = isLeaf; 
    } 
 
    // Internal node 
    public QuadNode(int val, boolean isLeaf, 
                    QuadNode tl, QuadNode tr, QuadNode bl, QuadNode br) { 
 
        this.val = val; 
        this.isLeaf = isLeaf; 
        this.topLeft = tl; 
        this.topRight = tr; 
        this.bottomLeft = bl; 
        this.bottomRight = br; 
 
    } 
} 
 
public class QuadTreeBuilder { 
 
    // Entry point 
    public QuadNode build(int[][] grid) { 
        return build(grid, 0, 0, grid.length); 
    } 
 
    // Recursive construction 
    private QuadNode build(int[][] grid, int r, int c, int size) { 
 
        // If region is uniform → create a leaf 
        if (isUniform(grid, r, c, size)) { 
            return new QuadNode(grid[r][c], true); 
        } 
 
        int half = size / 2; 
 
        // Split into 4 quadrants (recursively build) 
        QuadNode topLeft     = build(grid, r, c, half); 
        QuadNode topRight    = build(grid, r, c + half, half); 
        QuadNode bottomLeft  = build(grid, r + half, c, half); 
        QuadNode bottomRight = build(grid, r + half, c + half, half); 
 
        // Internal node (value is irrelevant) 
        return new QuadNode(-1, false, 
                            topLeft, topRight, bottomLeft, bottomRight); 
    } 
 
    // Check if region has the same value 
    private boolean isUniform(int[][] grid, int r, int c, int size) { 
        int v = grid[r][c]; 
        for (int i = r; i < r + size; i++) { 
            for (int j = c; j < c + size; j++) { 
                if (grid[i][j] != v) { 
                    return false; 
                } 
            } 
        } 
        return true; 
    } 
} 
