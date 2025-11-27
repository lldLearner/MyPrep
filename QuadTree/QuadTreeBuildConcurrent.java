import java.util.concurrent.RecursiveTask; 
 
class QuadNode { 
    int val; 
    boolean isLeaf; 
    QuadNode topLeft, topRight, bottomLeft, bottomRight; 
 
    QuadNode(int val, boolean isLeaf) { 
        this.val = val; 
        this.isLeaf = isLeaf; 
    } 
} 
 
class QuadBuildTask extends RecursiveTask<QuadNode> { 
 
 
    int[][] grid; 
    int r, c, size; 
 
    QuadBuildTask(int[][] grid, int r, int c, int size) { 
        this.grid = grid; 
        this.r = r; 
        this.c = c; 
        this.size = size; 
    } 
 
    @Override 
    protected QuadNode compute() { 
 
        if (isUniform(grid, r, c, size)) { 
            return new QuadNode(grid[r][c], true); 
        } 
 
        int half = size / 2; 
 
        QuadBuildTask tl = new QuadBuildTask(grid, r, c, half); 
        QuadBuildTask tr = new QuadBuildTask(grid, r, c + half, half); 
        QuadBuildTask bl = new QuadBuildTask(grid, r + half, c, half); 
        QuadBuildTask br = new QuadBuildTask(grid, r + half, c + half, half); 
 
        // Run 3 tasks asynchronously 
        tl.fork(); 
        tr.fork(); 
        bl.fork(); 
 
        // Compute one in current thread 
        QuadNode brNode = br.compute(); 
 
        // Wait for others to finish 
        QuadNode tlNode = tl.join(); 
        QuadNode trNode = tr.join(); 
        QuadNode blNode = bl.join(); 
 
        QuadNode root = new QuadNode(-1, false); 
        root.topLeft = tlNode; 
        root.topRight = trNode; 
        root.bottomLeft = blNode; 
        root.bottomRight = brNode; 
 
        return root; 
    } 
 
    private boolean isUniform(int[][] grid, int r, int c, int size) { 
        int v = grid[r][c]; 
        for (int i = r; i < r + size; i++) 
            for (int j = c; j < c + size; j++) 
                if (grid[i][j] != v) return false; 
        return true; 
    } 
