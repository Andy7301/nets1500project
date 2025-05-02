import java.util.*;

public class Algo {

    // Bfs on graph starting from vertex start, visits all reachable vertices, then continues from any unvisited vertex until all vertices are traversed.
    
    public static List<Integer> bfs(Graph g, int start) {
        List<Integer> order = new ArrayList<>();
        int n = g.getSize();
        // Return empty list if start index is out of bounds
        if (start < 0 || start >= n) {
            return order;
        }

        Set<Integer> seen = new HashSet<>();
        Queue<Integer> q = new ArrayDeque<>();

        // Initialize BFS
        seen.add(start);
        q.add(start);

        while (!q.isEmpty()) {
            int u = q.poll();
            order.add(u);

            // Sort neighbors 
            List<Integer> neighbors = new ArrayList<>(g.outNeighbors(u));
            Collections.sort(neighbors);
            for (int v : neighbors) {
                if (!seen.contains(v)) {
                    seen.add(v);
                    q.add(v);
                }
            }

            // If queue is empty but nodes remain unvisited, enqueue the next unvisited
            if (q.isEmpty() && seen.size() < n) {
                for (int i = 0; i < n; i++) {
                    if (!seen.contains(i)) {
                        seen.add(i);
                        q.add(i);
                        break;
                    }
                }
            }
        }
        return order;
    }

    // Dfs on graph starting from vertex start, uses a stack for traversal and continues to unvisited vertices after finishing a connected component.
 
    public static List<Integer> dfs(Graph g, int start) {
        List<Integer> order = new ArrayList<>();
        int n = g.getSize();
        if (start < 0 || start >= n) {
            return order;
        }

        Set<Integer> seen = new HashSet<>();
        Deque<Integer> stack = new ArrayDeque<>();

        // Begin DFS
        stack.push(start);
        while (!stack.isEmpty()) {
            int u = stack.pop();
            if (!seen.contains(u)) {
                seen.add(u);
                order.add(u);

                // Sort neighbors and push in reverse order
                List<Integer> neighbors = new ArrayList<>(g.outNeighbors(u));
                Collections.sort(neighbors);
                Collections.reverse(neighbors);
                for (int v : neighbors) {
                    if (!seen.contains(v)) {
                        stack.push(v);
                    }
                }
            }

            // If stack is empty but nodes remain unvisited, push next unvisited
            if (stack.isEmpty() && seen.size() < n) {
                for (int i = 0; i < n; i++) {
                    if (!seen.contains(i)) {
                        stack.push(i);
                        break;
                    }
                }
            }
        }
        return order;
    }

    // Computes the shortest path from src to dst

    public static List<Integer> dijkstra(Graph g, int src, int dst) {
        int n = g.getSize();
        int[] dist = new int[n];
        int[] prev = new int[n];
        boolean[] done = new boolean[n];

        // Initialize distances
        Arrays.fill(dist, Integer.MAX_VALUE);
        dist[src] = 0;
        prev[src] = -1;

        // Min heap of distance, vertex
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
                break; // Found shortest path to destination
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

        // Reconstruct path backwards
        List<Integer> path = new ArrayList<>();
        if (dist[dst] == Integer.MAX_VALUE) {
            return path; // No path found
        }
        for (int at = dst; at != -1; at = prev[at]) {
            path.add(0, at);
        }
        return path;
    }

    // Topo sort on a DAG, detects cycles and returns null if the graph is not a DAG.

    public static List<Integer> toposort(Graph g) {
        int n = g.getSize();
        boolean[] seen = new boolean[n];
        boolean[] onStack = new boolean[n];
        Deque<Integer> stack = new ArrayDeque<>();

        // Visit all vertices
        for (int i = 0; i < n; i++) {
            if (!seen[i]) {
                boolean acyclic = dfsTopoHelper(i, g, seen, onStack, stack);
                if (!acyclic) {
                    return null; // Cycle detected
                }
            }
        }

        // Pop stack 
        List<Integer> sort = new ArrayList<>();
        while (!stack.isEmpty()) {
            sort.add(stack.pop());
        }
        return sort;
    }

    // Helper for topo sort

    private static boolean dfsTopoHelper(int v, Graph g, boolean[] seen, boolean[] onStack, Deque<Integer> stack) {
        seen[v] = true;
        onStack[v] = true;

        // Sort neighbors 
        List<Integer> neighbors = new ArrayList<>(g.outNeighbors(v));
        Collections.sort(neighbors);
        for (int u : neighbors) {
            if (onStack[u]) {
                return false; // Cycle found
            }
            if (!seen[u]) {
                boolean acyclic = dfsTopoHelper(u, g, seen, onStack, stack);
                if (!acyclic) {
                    return false;
                }
            }
        }

        onStack[v] = false;
        stack.push(v);
        return true;
    }
}
