import java.util.*;

public class MumbaiMSTCO3CS {

    // ── Union-Find for Kruskal's MST ──────────────────────────────────────
    static int[] parent, rank;

    static void initUF(int n) {
        parent = new int[n];
        rank   = new int[n];
        for (int i = 0; i < n; i++) parent[i] = i;
    }

    static int find(int x) {
        if (parent[x] != x) parent[x] = find(parent[x]);
        return parent[x];
    }

    static boolean union(int a, int b) {
        int pa = find(a), pb = find(b);
        if (pa == pb) return false;
        if (rank[pa] < rank[pb]) { int t = pa; pa = pb; pb = t; }
        parent[pb] = pa;
        if (rank[pa] == rank[pb]) rank[pa]++;
        return true;
    }

    // ── Bridge detection via Tarjan's algorithm ───────────────────────────
    static int timer;
    static int[] disc, low;
    static boolean[] visited;
    static List<List<int[]>> adj; // adj[u] = list of {v, edgeIndex}
    static List<int[]> bridges;  // each = {u, v, cost}

    static void dfs(int u, int parentEdge) {
        visited[u] = true;
        disc[u] = low[u] = timer++;
        for (int[] nb : adj.get(u)) {
            int v = nb[0], eidx = nb[1];
            if (!visited[v]) {
                dfs(v, eidx);
                low[u] = Math.min(low[u], low[v]);
                if (low[v] > disc[u]) {
                    bridges.add(nb); // nb = {v, eidx} — store edge endpoint
                }
            } else if (eidx != parentEdge) {
                low[u] = Math.min(low[u], disc[v]);
            }
        }
    }

    static List<int[]> findBridges(int n, List<int[]> edges) {
        adj = new ArrayList<>();
        for (int i = 0; i < n; i++) adj.add(new ArrayList<>());
        for (int i = 0; i < edges.size(); i++) {
            int u = edges.get(i)[0], v = edges.get(i)[1];
            adj.get(u).add(new int[]{v, i});
            adj.get(v).add(new int[]{u, i});
        }
        disc = new int[n]; low = new int[n];
        visited = new boolean[n];
        bridges = new ArrayList<>();
        timer = 0;
        for (int i = 0; i < n; i++)
            if (!visited[i]) dfs(i, -1);
        return bridges;
    }

    public static void main(String[] args) {
        // Node mapping: M=0, A=1, B=2, C=3, D=4, E=5, F=6, G=7
        String[] name = {"M","A","B","C","D","E","F","G"};
        int N = 8;

        // All 12 candidate edges: {u, v, cost}
        int[][] allEdges = {
            {1,2,2},  // A-B
            {1,0,4},  // A-M
            {1,4,7},  // A-D
            {2,3,2},  // B-C
            {2,0,3},  // B-M
            {0,3,5},  // M-C
            {0,5,4},  // M-E
            {0,7,6},  // M-G
            {3,7,3},  // C-G
            {4,5,3},  // D-E
            {5,6,2},  // E-F
            {6,7,4}   // F-G
        };

        System.out.println("=== Mumbai Distribution Grid — N-1 Redundancy via Bridge Analysis ===");
        System.out.println();
        System.out.println("Nodes : M A B C D E F G  (8 total)");
        System.out.println("Candidate edges (12 total):");
        System.out.printf("  %-8s %-8s %s%n","Edge","Nodes","Cost(cr)");
        System.out.println("  " + "-".repeat(28));
        String[] edgeNames = {"A-B","A-M","A-D","B-C","B-M","M-C","M-E","M-G","C-G","D-E","E-F","F-G"};
        for (int i = 0; i < allEdges.length; i++) {
            System.out.printf("  %-8s (%s - %s)   %d%n",
                edgeNames[i], name[allEdges[i][0]], name[allEdges[i][1]], allEdges[i][2]);
        }

        // ── Step 1: Kruskal's MST ─────────────────────────────────────────
        System.out.println();
        System.out.println("--- Step 1: Kruskal's MST ---");
        int[][] sorted = allEdges.clone();
        Arrays.sort(sorted, (a,b) -> a[2]-b[2]);

        initUF(N);
        List<int[]> mstEdges = new ArrayList<>();
        int mstCost = 0;
        System.out.println("Processing edges in cost order:");
        for (int[] e : sorted) {
            boolean added = union(e[0], e[1]);
            System.out.printf("  %s-%s (cost=%d) -> %s%n",
                name[e[0]], name[e[1]], e[2], added ? "ADDED to MST" : "skipped (cycle)");
            if (added) { mstEdges.add(e); mstCost += e[2]; }
        }
        System.out.println();
        System.out.println("MST edges (" + mstEdges.size() + " edges):");
        for (int[] e : mstEdges)
            System.out.printf("  %s-%s  cost=%d%n", name[e[0]], name[e[1]], e[2]);
        System.out.println("  Total MST cost = " + mstCost + " crore");

        // ── Step 2: Every MST edge is a bridge — find bridges ────────────
        System.out.println();
        System.out.println("--- Step 2: Bridge Detection on MST (Tarjan's Algorithm) ---");
        System.out.println("Every edge in an MST is a bridge by definition.");
        System.out.println("Bridges found in MST:");
        List<int[]> mstBridges = findBridges(N, mstEdges);
        for (int[] b : mstBridges) {
            // b = {v, edgeIndex}
            int[] e = mstEdges.get(b[1]);
            System.out.printf("  Bridge: %s-%s (cost=%d)%n", name[e[0]], name[e[1]], e[2]);
        }

        // ── Step 3: Augment — add cheapest non-MST edges to cover bridges ─
        System.out.println();
        System.out.println("--- Step 3: Augmentation — Eliminate All Bridges ---");
        System.out.println("Strategy: for each bridge, add the cheapest non-MST edge");
        System.out.println("  whose cycle covers (closes) that bridge.");
        System.out.println();

        // Non-MST edges
        Set<String> mstSet = new HashSet<>();
        for (int[] e : mstEdges)
            mstSet.add(Math.min(e[0],e[1])+"-"+Math.max(e[0],e[1]));

        List<int[]> nonMST = new ArrayList<>();
        for (int[] e : allEdges) {
            String key = Math.min(e[0],e[1])+"-"+Math.max(e[0],e[1]);
            if (!mstSet.contains(key)) nonMST.add(e);
        }
        System.out.println("Non-MST (candidate augmentation) edges:");
        for (int[] e : nonMST)
            System.out.printf("  %s-%s  cost=%d%n", name[e[0]], name[e[1]], e[2]);

        // For each non-MST edge, it covers the unique path between its endpoints in MST
        // Build MST adjacency for path queries
        List<List<int[]>> mstAdj = new ArrayList<>();
        for (int i = 0; i < N; i++) mstAdj.add(new ArrayList<>());
        for (int i = 0; i < mstEdges.size(); i++) {
            int u = mstEdges.get(i)[0], v = mstEdges.get(i)[1];
            mstAdj.get(u).add(new int[]{v, i});
            mstAdj.get(v).add(new int[]{u, i});
        }

        // BFS to find path in MST between two nodes -> returns set of edge indices covered
        // Greedy: sort non-MST edges by cost, add if it covers at least one uncovered bridge
        Set<Integer> uncoveredBridges = new HashSet<>();
        Map<Integer,String> bridgeEdgeStr = new HashMap<>();
        for (int[] b : mstBridges) {
            uncoveredBridges.add(b[1]);
            int[] e = mstEdges.get(b[1]);
            bridgeEdgeStr.put(b[1], name[e[0]]+"-"+name[e[1]]);
        }

        List<int[]> augEdges = new ArrayList<>();
        int augCost = 0;
        List<int[]> nonMSTsorted = new ArrayList<>(nonMST);
        nonMSTsorted.sort((a,b) -> a[2]-b[2]);

        for (int[] e : nonMSTsorted) {
            if (uncoveredBridges.isEmpty()) break;
            Set<Integer> covered = pathEdges(e[0], e[1], N, mstAdj);
            Set<Integer> inter = new HashSet<>(covered);
            inter.retainAll(uncoveredBridges);
            if (!inter.isEmpty()) {
                System.out.printf("  Add %s-%s (cost=%d): covers bridges ",
                    name[e[0]], name[e[1]], e[2]);
                for (int idx : inter) System.out.print(bridgeEdgeStr.get(idx)+" ");
                System.out.println();
                augEdges.add(e);
                augCost += e[2];
                uncoveredBridges.removeAll(inter);
            }
        }

        System.out.println();
        System.out.println("=== FINAL RESULT ===");
        System.out.println("MST edges:");
        for (int[] e : mstEdges)
            System.out.printf("  %s-%s  cost=%d%n", name[e[0]], name[e[1]], e[2]);
        System.out.println("Augmentation edges added:");
        for (int[] e : augEdges)
            System.out.printf("  %s-%s  cost=%d%n", name[e[0]], name[e[1]], e[2]);
        System.out.println();
        System.out.println("MST cost        = " + mstCost + " crore");
        System.out.println("Augmentation    = " + augCost + " crore");
        System.out.println("Total network   = " + (mstCost+augCost) + " crore");
        System.out.println("Bridges remaining = " + uncoveredBridges.size()
            + " (N-1 redundancy " + (uncoveredBridges.isEmpty() ? "ACHIEVED" : "NOT MET") + ")");
    }

    // BFS in MST to find set of edge-indices on path from src to dst
    static Set<Integer> pathEdges(int src, int dst, int N, List<List<int[]>> mstAdj) {
        int[] prev = new int[N];
        int[] prevEdge = new int[N];
        Arrays.fill(prev, -1);
        Queue<Integer> q = new LinkedList<>();
        q.add(src); prev[src] = src;
        while (!q.isEmpty()) {
            int u = q.poll();
            if (u == dst) break;
            for (int[] nb : mstAdj.get(u)) {
                if (prev[nb[0]] == -1) {
                    prev[nb[0]] = u;
                    prevEdge[nb[0]] = nb[1];
                    q.add(nb[0]);
                }
            }
        }
        Set<Integer> edgeSet = new HashSet<>();
        int cur = dst;
        while (cur != src) {
            edgeSet.add(prevEdge[cur]);
            cur = prev[cur];
        }
        return edgeSet;
    }
}
