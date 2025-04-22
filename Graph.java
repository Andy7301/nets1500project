import java.util.*;

public class Graph {
    private final int size;
    private final Map<Integer, Integer>[] list;
    public Graph(int n) {
        if (n <= 0) { 
            throw new IllegalArgumentException();
        }
        size = n;
        list = new HashMap[n];
        for (int i = 0; i < n; i++) {
            list[i] = new HashMap<>();
        }
    }
    public int getSize() { 
        return size; 
    }
    public boolean hasEdge(int u, int v) {
        if (u < 0 || v < 0 || u >= size || v >= size) {
            throw new IllegalArgumentException();
        }
        return list[u].containsKey(v);
    }
    public int getWeight(int u, int v) {
        if (!hasEdge(u, v)) {
            throw new NoSuchElementException();
        }
        return list[u].get(v);
    }
    public boolean addEdge(int u, int v, int w) {
        if (u < 0 || v < 0 || u >= size || v >= size || u == v) {
            throw new IllegalArgumentException();
        }
        if (list[u].containsKey(v)) {
            return false;
        }
        list[u].put(v, w);
        return true;
    }
    public Set<Integer> outNeighbors(int v) {
        if (v < 0 || v >= size) {
            throw new IllegalArgumentException();
        }
        return list[v].keySet();
    }
}
