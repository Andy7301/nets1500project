import java.util.*;

public class Graph {

    private final int size;
    private final Map<Integer, Integer>[] adjacency;

    // Constructs a Graph with n vertices and no edges.

    public Graph(int n) {
        if (n <= 0) {
            throw new IllegalArgumentException("Graph size must be positive");
        }
        size = n;
        @SuppressWarnings("unchecked")
        Map<Integer, Integer>[] temp = new HashMap[n];
        adjacency = temp;
        for (int i = 0; i < size; i++) {
            adjacency[i] = new HashMap<>();
        }
    }

    // Returns the number of vertices in the graph.

    public int getSize() {
        return size;
    }

    // Checks whether there is an edge from u to v.
    public boolean hasEdge(int u, int v) {
        if (u < 0 || v < 0 || u >= size || v >= size) {
            throw new IllegalArgumentException("Vertex index out of range");
        }
        return adjacency[u].containsKey(v);
    }

    // Returns the weight of the edge from u to v. 
    public int getWeight(int u, int v) {
        if (!hasEdge(u, v)) {
            throw new NoSuchElementException("No edge from " + u + " to " + v);
        }
        return adjacency[u].get(v);
    }

    // Adds a directed edge from u to v with weight w.
     
    public boolean addEdge(int u, int v, int w) {
        if (u < 0 || v < 0 || u >= size || v >= size) {
            throw new IllegalArgumentException("Vertex index out of range");
        }
        if (u == v) {
            throw new IllegalArgumentException("No self-loops allowed");
        }
        if (w < 0) {
            throw new IllegalArgumentException("Weight must be non-negative");
        }
        if (adjacency[u].containsKey(v)) {
            return false;
        }
        adjacency[u].put(v, w);
        return true;
    }

    // Returns the set of neighbors reachable from v.

    public Set<Integer> outNeighbors(int v) {
        if (v < 0 || v >= size) {
            throw new IllegalArgumentException("Vertex index out of range");
        }
        return Collections.unmodifiableSet(adjacency[v].keySet());
    }

    // Updates the weight of an existing edge u-v.

    public void setWeight(int u, int v, int w) {
        if (u < 0 || v < 0 || u >= size || v >= size) {
            throw new IllegalArgumentException("Vertex index out of range");
        }
        if (w < 0) {
            throw new IllegalArgumentException("Weight must be non-negative");
        }
        if (!adjacency[u].containsKey(v)) {
            throw new NoSuchElementException("No edge from " + u + " to " + v);
        }
        adjacency[u].put(v, w);
    }

    // Removes all edges from the graph, preserving vertex count.
     
    public void clear() {
        for (Map<Integer, Integer> neighbors : adjacency) {
            neighbors.clear();
        }
    }
}
