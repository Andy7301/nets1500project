import java.util.*;

public class Algo {
    public static List<Integer> bfs(Graph g, int start) {
        List<Integer> order = new ArrayList<>();
        int n = g.getSize();
        if (start < 0 || start >= n) {
            return order;
        }
        Set<Integer> seen  = new HashSet<>();
        Queue<Integer> q = new ArrayDeque<>();
        seen.add(start);
        q.add(start);
        while (!q.isEmpty()) {
            int u = q.poll();
            order.add(u);
            List<Integer> neighbors = new ArrayList<>(g.outNeighbors(u));
            Collections.sort(neighbors);
            for (int v : neighbors) {
                if (!seen.contains(v)) {
                    seen.add(v);
                    q.add(v);
                }
            }
        }
        return order;
    }

    public static List<Integer> dfs(Graph g, int start) {
        List<Integer> order = new ArrayList<>();
        int n = g.getSize();
        if (start < 0 || start >= n) {
            return order;
        }
        Set<Integer> seen = new HashSet<>();
        Deque<Integer> stack = new ArrayDeque<>();
        stack.push(start);
        while (!stack.isEmpty()) {
            int u = stack.pop();
            if (!seen.contains(u)) {
                seen.add(u);
                order.add(u);
                List<Integer> neighbors = new ArrayList<>(g.outNeighbors(u));
                Collections.sort(neighbors);
                Collections.reverse(neighbors);
                for (int v : neighbors) {
                    if (!seen.contains(v)) {
                        stack.push(v);
                    }
                }
            }
        }
        return order;
    }

    public static List<Integer> dijkstra(Graph g, int src, int dst) {
        int n = g.getSize();
        int[] dist = new int[n];
        int[] prev = new int[n];
        boolean[] done = new boolean[n];
        Arrays.fill(dist, Integer.MAX_VALUE);
        dist[src] = 0;
        prev[src] = -1;
        PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt(a -> a[0]));
        pq.add(new int[]{0, src});
        while (!pq.isEmpty()) {
            int[] cur = pq.poll();
            int u = cur[1];
            if (done[u]) {
                continue;
            }
            done[u] = true;
            if (u == dst) {
                break;
            }
            for (int v : g.outNeighbors(u)) {
                int alt = dist[u] + g.getWeight(u, v);
                if (alt < dist[v]) {
                    dist[v] = alt;
                    prev[v] = u;
                    pq.add(new int[]{alt, v});
                }
            }
        }
        List<Integer> path = new ArrayList<>();
        if (dist[dst] == Integer.MAX_VALUE) {
            return path;
        }
        for (int at = dst; at != -1; at = prev[at]) {
            path.add(0, at);
        }
        return path;
    }

    public static List<Integer> toposort(Graph g) {
        int n = g.getSize();
        boolean[] seen = new boolean[n];
        Deque<Integer> stack = new ArrayDeque<>();

        for (int i = 0; i < n; i++) {
            dfsTopoHelper(i, g, seen, stack);
        }

        List<Integer> sort = new ArrayList<>();
        while (!stack.isEmpty()) {
            sort.add(stack.pop());
        }

        return sort;
    }

    private static void dfsTopoHelper(int v, Graph g, boolean[] seen, Deque<Integer> stack) {
        seen[v] = true;

        for (int neighbor : g.outNeighbors(v)) {
            if (!seen[neighbor]) {
                dfsTopoHelper(neighbor, g, seen, stack);
            }
        }
        stack.push(v);
    }
}
