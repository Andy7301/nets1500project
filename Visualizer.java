import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.awt.Point;
import javax.swing.Timer;

public class Visualizer extends JPanel implements MouseListener {

    private final List<Point> nodes;
    private final List<int[]> edges;
    private final Graph graph;
    private final int radius = 20;
    private int selectedNode = -1;

    private List<Integer> highlightedPath = new ArrayList<>();
    private Timer timer;
    private int highlightIndex = 0;

    private static final int MAX_NODES = 100; // Up to 100 nodes

    public Visualizer() {
        this.nodes = new ArrayList<>();
        this.edges = new ArrayList<>();
        this.graph = new Graph(MAX_NODES);
        addMouseListener(this);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        setBackground(Color.WHITE);

        // Draw edges
        g.setColor(Color.BLACK);
        for (int[] edge : edges) {
            Point p1 = nodes.get(edge[0]);
            Point p2 = nodes.get(edge[1]);
            g.drawLine(p1.x, p1.y, p2.x, p2.y);
        }

        // Draw nodes
        for (int i = 0; i < nodes.size(); i++) {
            Point p = nodes.get(i);
            if (highlightedPath.contains(i)) {
                g.setColor(Color.ORANGE);
            } else {
                g.setColor(Color.CYAN);
            }
            g.fillOval(p.x - radius, p.y - radius, 2 * radius, 2 * radius);
            g.setColor(Color.BLACK);
            g.drawOval(p.x - radius, p.y - radius, 2 * radius, 2 * radius);
            g.drawString(Integer.toString(i), p.x - 5, p.y + 5);
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        Point click = e.getPoint();
        int nodeIdx = getNodeAt(click);

        if (nodeIdx == -1) {
            // No node clicked -> Add a new node
            if (nodes.size() < MAX_NODES) {
                nodes.add(click);
                repaint();
            } else {
                JOptionPane.showMessageDialog(this, "Max number of nodes reached!");
            }
        } else {
            // Node clicked
            if (selectedNode == -1) {
                selectedNode = nodeIdx;
            } else {
                if (selectedNode != nodeIdx && !graph.hasEdge(selectedNode, nodeIdx)) {
                    graph.addEdge(selectedNode, nodeIdx, 1); // Add undirected edges
                    graph.addEdge(nodeIdx, selectedNode, 1); // <-- key line (both ways)
                    edges.add(new int[]{selectedNode, nodeIdx});
                }
                selectedNode = -1;
                repaint();
            }
        }
    }

    private int getNodeAt(Point p) {
        for (int i = 0; i < nodes.size(); i++) {
            Point node = nodes.get(i);
            if (node.distance(p) <= radius) {
                return i;
            }
        }
        return -1;
    }

    public void visualize(List<Integer> path) {
        highlightedPath.clear();
        highlightIndex = 0;
        if (timer != null) {
            timer.stop();
        }

        timer = new Timer(500, e -> {
            if (highlightIndex < path.size()) {
                highlightedPath.add(path.get(highlightIndex));
                highlightIndex++;
                repaint();
            } else {
                ((Timer) e.getSource()).stop();
            }
        });
        timer.start();
    }

    public Graph getGraph() {
        return graph;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Visualizer panel = new Visualizer();

            JFrame frame = new JFrame("Graph Visualizer");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 600);
            frame.add(panel, BorderLayout.CENTER);

            JPanel buttons = new JPanel();
            JButton bfsButton = new JButton("BFS");
            JButton dfsButton = new JButton("DFS");
            JButton dijkstraButton = new JButton("Dijkstra");
            JButton topoButton = new JButton("Topological Sort");

            bfsButton.addActionListener(e -> {
                String input = JOptionPane.showInputDialog("Start node for BFS:");
                int start = Integer.parseInt(input);
                List<Integer> order = Algo.bfs(panel.getGraph(), start);
                panel.visualize(order);
            });

            dfsButton.addActionListener(e -> {
                String input = JOptionPane.showInputDialog("Start node for DFS:");
                int start = Integer.parseInt(input);
                List<Integer> order = Algo.dfs(panel.getGraph(), start);
                panel.visualize(order);
            });

            dijkstraButton.addActionListener(e -> {
                int src = Integer.parseInt(JOptionPane.showInputDialog("Source node for Dijkstra:"));
                int dst = Integer.parseInt(JOptionPane.showInputDialog("Destination node for Dijkstra:"));
                List<Integer> path = Algo.dijkstra(panel.getGraph(), src, dst);
                panel.visualize(path);
            });

            topoButton.addActionListener(e -> {
                List<Integer> order = topoSort(panel.getGraph());
                if (order == null) {
                    JOptionPane.showMessageDialog(panel, "Graph is not a DAG. Topological sort not possible.");
                } else {
                    panel.visualize(order);
                }
            });

            buttons.add(bfsButton);
            buttons.add(dfsButton);
            buttons.add(dijkstraButton);
            buttons.add(topoButton);

            frame.add(buttons, BorderLayout.SOUTH);
            frame.setVisible(true);
        });
    }

    private static List<Integer> topoSort(Graph g) {
        int n = g.getSize();
        int[] inDegree = new int[n];
        for (int u = 0; u < n; u++) {
            for (int v : g.outNeighbors(u)) {
                inDegree[v]++;
            }
        }

        Queue<Integer> q = new ArrayDeque<>();
        for (int i = 0; i < n; i++) {
            if (inDegree[i] == 0) {
                q.add(i);
            }
        }

        List<Integer> order = new ArrayList<>();
        while (!q.isEmpty()) {
            int u = q.poll();
            order.add(u);
            for (int v : g.outNeighbors(u)) {
                inDegree[v]--;
                if (inDegree[v] == 0) {
                    q.add(v);
                }
            }
        }

        if (order.size() != n) {
            return null; // not a DAG
        }
        return order;
    }

    // Empty MouseListener methods
    @Override
    public void mouseClicked(MouseEvent e) {}

    @Override
    public void mouseReleased(MouseEvent e) {}

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}
}
