Rectangle 
 
QuadNode build(int[][] grid, int r1, int c1, int r2, int c2) { 
    if (isUniform(grid, r1, c1, r2, c2)) { 
        return new QuadNode(grid[r1][c1], true); 
    } 
 
 
    int rm = (r1 + r2) / 2; 
    int cm = (c1 + c2) / 2; 
 
    QuadNode tl = build(grid, r1, c1, rm, cm); 
    QuadNode tr = build(grid, r1, cm, rm, c2); 
    QuadNode bl = build(grid, rm, c1, r2, cm); 
    QuadNode br = build(grid, rm, cm, r2, c2); 
 
    return new QuadNode(0, false, tl, tr, bl, br); 
} 
 
// Check if the rectangular region is uniform 
    boolean isUniform(int[][] grid, int r1, int c1, int r2, int c2) { 
        int val = grid[r1][c1]; 
 
        for (int r = r1; r < r2; r++) { 
            for (int c = c1; c < c2; c++) { 
                if (grid[r][c] != val) { 
                    return false; 
                } 
            } 
        } 
        return true; 
    }
