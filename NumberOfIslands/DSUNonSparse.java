package ParkingSpot;

public class Terrain {

    private int row, col;
    private int[][] terrain;
    private DSU dsu;
    private int countIslands = 0;

    public Terrain(int row, int col) {
        this.row = row;
        this.col = col;
        this.terrain = new int[row][col];
        this.dsu = new DSU(row * col);
    }

    private int getIndex(int x, int y) {
        return x * col + y;
    }

    public boolean isLand(int x, int y) {
        return terrain[x][y] == 1;
    }

    // Dynamic island counting using DSU
    public int addLand(int x, int y) {
        if (terrain[x][y] == 1) {
            return countIslands; // already land
        }

        terrain[x][y] = 1;
        countIslands++;  // new island initially
        int idx1 = getIndex(x, y);

        int[] dx = {0, 0, 1, -1};
        int[] dy = {1, -1, 0, 0};

        for (int i = 0; i < 4; i++) {
            int nx = x + dx[i];
            int ny = y + dy[i];

            if (nx >= 0 && ny >= 0 && nx < row && ny < col && terrain[nx][ny] == 1) {
                int idx2 = getIndex(nx, ny);

                // If union is successful, reduce islands
                if (dsu.union(idx1, idx2)) {
                    countIslands--;
                }
            }
        }

        return countIslands;
    }

    public int getIslands() {
        return countIslands;
    }

    // ------------------------------------------
    // DSU Class
    // ------------------------------------------
    static class DSU {
        int[] parent, rank;

        DSU(int n) {
            parent = new int[n];
            rank = new int[n];
            for (int i = 0; i < n; i++) parent[i] = i;
        }

        int find(int x) {
            if (parent[x] != x) parent[x] = find(parent[x]); // path compression
            return parent[x];
        }

        boolean union(int x, int y) {
            int px = find(x);
            int py = find(y);
            if (px == py) return false; // already connected

            if (rank[px] < rank[py]) {
                parent[px] = py;
            } else if (rank[px] > rank[py]) {
                parent[py] = px;
            } else {
                parent[py] = px;
                rank[px]++;
            }
            return true;
        }
    }
}
