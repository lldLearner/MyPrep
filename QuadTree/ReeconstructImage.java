Reconstruct image 
 
void fill(QuadNode node, int[][] grid, int r1, int c1, int r2, int c2) { 
    if (node.isLeaf) { 
        for (int i = r1; i < r2; i++) 
            for (int j = c1; j < c2; j++) 
                grid[i][j] = node.val; 
        return; 
    } 
 
    int rm = (r1 + r2) / 2; 
    int cm = (c1 + c2) / 2; 
 
    fill(node.topLeft,     grid, r1, c1,  rm, cm); 
    fill(node.topRight,    grid, r1, cm,  rm, c2); 
    fill(node.bottomLeft,  grid, rm, c1,  r2, cm); 
    fill(node.bottomRight, grid, rm, cm,  r2, c2); 
} 
int[][] output = new int[h][w]; 
fill(root, output, 0, 0, h, w); 
