import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class Visualizer extends JPanel implements MouseListener {

    private final List<Point> nodes;
    private final List<int[]> edges;
    private final Graph graph;
    private final int radius = 20;
    private int selectedNode = -1;

    private List<Integer> fullPath = new ArrayList<>();
    private List<Integer> highlightedPath = new ArrayList<>();
    private Timer timer;
    private int highlightIndex = -1;

    public void setPath(List<Integer> path) {
        fullPath = new ArrayList<>(path);
        highlightedPath.clear();
        highlightIndex = -1;
        repaint();
    }
    
    public void nextStep() {
        if (highlightIndex < fullPath.size() - 1) {
            highlightIndex++;
            highlightedPath.add(fullPath.get(highlightIndex));
            repaint();
        }
    }
    
    public void prevStep() {
        if (highlightIndex >= 0) {
            highlightedPath.remove(highlightIndex);
            highlightIndex--;
            repaint();
        }
    }
    
    public int getCurrentNode() {
        if (highlightIndex >= 0 && highlightIndex < highlightedPath.size())
            return highlightedPath.get(highlightIndex);
        return -1;
    }
    
    public int getStepNumber() {
        return highlightIndex + 1;  // 1-based
    }

    private static final int MAX_NODES = 100; // Up to 100 nodes

    public Visualizer() {
        this.nodes = new ArrayList<>();
        this.edges = new ArrayList<>();
        this.graph = new Graph(MAX_NODES);
        addMouseListener(this);
    }

    @Override
    protected void paintComponent(Graphics g) {
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
                    graph.addEdge(nodeIdx, selectedNode, 1); 
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

            JButton prevButton = new JButton("Prev Step");
            JButton nextButton = new JButton("Next Step");

            JTextArea explanation = new JTextArea(4, 20);

            explanation.setEditable(false);
            explanation.setLineWrap(true);
            explanation.setWrapStyleWord(true);

            explanation.setText(
                "Instructions:\n\n" +
                "• Click on empty space to add a node.\n" +
                "• Click two nodes to connect them with an edge.\n" +
                "• Buttons below let you visualize BFS, DFS, Dijkstra, or Topological Sort.\n" +
                "• After selecting an algorithm, use the Prev Step  and Next Step buttons to walk through each step.\n" +
                "• Each step will highlight visited nodes, and display the current node.\n\n" +
                "Start by adding nodes and edges!"
            );

            bfsButton.addActionListener(e -> {
                String input = JOptionPane.showInputDialog("Start node for BFS:");
                int start = Integer.parseInt(input);
                List<Integer> order = Algo.bfs(panel.getGraph(), start);
                panel.setPath(order);
                explanation.setText(
                    "Breadth-First Search (BFS)\n" +
                    "1) enqueue start node\n" +
                    "2) while queue not empty:\n" +
                    "     • dequeue u, visit\n" +
                    "     • enqueue all unvisited neighbors\n" +
                    "Use Next/Prev to step."
                );
            });

            dfsButton.addActionListener(e -> {
                String input = JOptionPane.showInputDialog("Start node for DFS:");
                int start = Integer.parseInt(input);
                List<Integer> order = Algo.dfs(panel.getGraph(), start);
                panel.setPath(order);
                explanation.setText(
                    "Depth-First Search (DFS)\n" +
                    "1) push start on stack\n" +
                    "2) while stack not empty:\n" +
                    "     • pop u, visit\n" +
                    "     • push all unvisited neighbors\n" +
                    "Use Next/Prev to step."
                );
            });

            dijkstraButton.addActionListener(e -> {
                int src = Integer.parseInt(JOptionPane.showInputDialog("Source node for Dijkstra:"));
                int dst = Integer.parseInt(JOptionPane.showInputDialog("Destination node for Dijkstra:"));
                List<Integer> path = Algo.dijkstra(panel.getGraph(), src, dst);
                panel.setPath(path);
                explanation.setText(
                    "Dijkstra’s Shortest Path\n" +
                    "1) initialize dist[src]=0, others=∞\n" +
                    "2) extract-min u from PQ, relax all edges (u→v)\n" +
                    "3) repeat until dst extracted\n" +
                    "Use Next/Prev to see each node in the final path."
                );
            });

            topoButton.addActionListener(e -> {
                List<Integer> order = Algo.toposort(panel.getGraph());
                if (order == null) {
                    JOptionPane.showMessageDialog(panel, "Graph is not a DAG. Topological sort not possible.");
                } else {
                    panel.setPath(order);
                    explanation.setText(
                        "Topological Sort via DFS:\n" +
                        "1) run DFS from every unvisited node\n" +
                        "2) on return push node onto stack\n" +
                        "3) pop stack for final order\n" +
                        "Use Next/Prev to step."
                    );
                }
            });

            prevButton.addActionListener(e -> {
                panel.prevStep();
                int node = panel.getCurrentNode();
                explanation.setText("Step " + panel.getStepNumber() +
                    ": visited node " + node);
            });
            nextButton.addActionListener(e -> {
                panel.nextStep();
                int node = panel.getCurrentNode();
                explanation.setText("Step " + panel.getStepNumber() +
                    ": visited node " + node);
            });

            buttons.add(bfsButton);
            buttons.add(dfsButton);
            buttons.add(dijkstraButton);
            buttons.add(topoButton);
            buttons.add(prevButton);
            buttons.add(nextButton);    

            frame.add(buttons, BorderLayout.SOUTH);
            frame.add(new JScrollPane(explanation), BorderLayout.EAST);
            frame.setVisible(true);
        });
    }

    // Empty MouseListener methods
    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
}