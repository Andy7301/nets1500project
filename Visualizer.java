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


    private static final int MAX_NODES = 100;
    private boolean isDirected = false;

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
            drawEdge(g, p1, p2, isDirected);
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

    private void drawEdge(Graphics g, Point p1, Point p2, boolean directed) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setStroke(new BasicStroke(2));

        double dx = p2.x - p1.x;
        double dy = p2.y - p1.y;
        double dist = Math.sqrt(dx * dx + dy * dy);

        if (dist == 0) return; // Avoid divide-by-zero if points are identical

        // Scale the vector to radius length
        double offsetX = dx * radius / dist;
        double offsetY = dy * radius / dist;

        // Compute shifted points
        int startX = (int) (p1.x + offsetX);
        int startY = (int) (p1.y + offsetY);
        int endX = (int) (p2.x - offsetX);
        int endY = (int) (p2.y - offsetY);

        // Draw the line
        g2.drawLine(startX, startY, endX, endY);

        if (directed) {
            double phi = Math.toRadians(30);
            int barb = 10;

            double theta = Math.atan2(endY - startY, endX - startX);

            int x, y;
            for (int j = 0; j < 2; j++) {
                double angle = theta + (j == 0 ? phi : -phi);
                x = (int) (endX - barb * Math.cos(angle));
                y = (int) (endY - barb * Math.sin(angle));
                g2.drawLine(endX, endY, x, y);
            }
        }
    }


    @Override
    public void mousePressed(MouseEvent e) {
        Point click = e.getPoint();
        int nodeIdx = getNodeAt(click);

        if (nodeIdx == -1) {
            if (nodes.size() < MAX_NODES) {
                nodes.add(click);
                repaint();
            } else {
                JOptionPane.showMessageDialog(this, "Max number of nodes reached!");
            }
        } else {
            if (selectedNode == -1) {
                selectedNode = nodeIdx;
            } else {
                if (selectedNode != nodeIdx && !graph.hasEdge(selectedNode, nodeIdx)) {
                    graph.addEdge(selectedNode, nodeIdx, 1);
                    if (!isDirected) {
                        graph.addEdge(nodeIdx, selectedNode, 1);
                    }
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
        return highlightIndex + 1;
    }

    public Graph getGraph() {
        return graph;
    }

    public void toggleDirected() {
        isDirected = !isDirected;
        repaint();
    }

    public void resetAll() {
        nodes.clear();
        edges.clear();
        fullPath.clear();
        highlightedPath.clear();
        selectedNode = -1;
        highlightIndex = -1;
        repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Visualizer panel = new Visualizer();

            JFrame frame = new JFrame("Graph Visualizer");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1000, 700);
            frame.add(panel, BorderLayout.CENTER);

            JPanel sidebar = new JPanel();
            sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.X_AXIS));

            JButton bfsButton = new JButton("BFS");
            JButton dfsButton = new JButton("DFS");
            JButton dijkstraButton = new JButton("Dijkstra");
            JButton topoButton = new JButton("Topological Sort");

            JButton prevButton = new JButton("Prev Step");
            JButton nextButton = new JButton("Next Step");
            JButton toggleButton = new JButton("Toggle Directed/Undirected");
            JButton resetButton = new JButton("Reset All");
            JLabel modeLabel = new JLabel("Current Mode: Undirected");


            JTextArea explanation = new JTextArea(8, 20);
            explanation.setEditable(false);
            explanation.setLineWrap(true);
            explanation.setWrapStyleWord(true);

            explanation.setText(
                    "Instructions:\n\n" +
                            "• Click to add nodes.\n" +
                            "• Click two nodes to connect them.\n" +
                            "• Toggle directed/undirected mode anytime.\n" +
                            "• Reset to start over.\n" +
                            "• Visualize BFS, DFS, Dijkstra, or Topological Sort."
            );

            bfsButton.addActionListener(e -> {
                String input = JOptionPane.showInputDialog("Start node for BFS:");
                int start = Integer.parseInt(input);
                List<Integer> order = Algo.bfs(panel.getGraph(), start);
                panel.setPath(order);
            });

            dfsButton.addActionListener(e -> {
                String input = JOptionPane.showInputDialog("Start node for DFS:");
                int start = Integer.parseInt(input);
                List<Integer> order = Algo.dfs(panel.getGraph(), start);
                panel.setPath(order);
            });

            dijkstraButton.addActionListener(e -> {
                int src = Integer.parseInt(JOptionPane.showInputDialog("Source node for Dijkstra:"));
                int dst = Integer.parseInt(JOptionPane.showInputDialog("Destination node for Dijkstra:"));
                List<Integer> path = Algo.dijkstra(panel.getGraph(), src, dst);
                panel.setPath(path);
            });

            topoButton.addActionListener(e -> {
                List<Integer> order = Algo.toposort(panel.getGraph());
                if (order == null) {
                    JOptionPane.showMessageDialog(panel, "Graph is not a DAG. Topological sort not possible.");
                } else {
                    panel.setPath(order);
                }
            });

            prevButton.addActionListener(e -> panel.prevStep());
            nextButton.addActionListener(e -> panel.nextStep());
            toggleButton.addActionListener(e -> {
                panel.toggleDirected();
                String mode = panel.isDirected ? "DIRECTED" : "UNDIRECTED";
                JOptionPane.showMessageDialog(panel, "Graph is now " + mode + "!");
                modeLabel.setText("Current Mode:" + mode);
            });
            resetButton.addActionListener(e -> {
                panel.resetAll();
            });


            sidebar.add(bfsButton);
            sidebar.add(dfsButton);
            sidebar.add(dijkstraButton);
            sidebar.add(topoButton);
            sidebar.add(prevButton);
            sidebar.add(nextButton);
            sidebar.add(toggleButton);
            sidebar.add(resetButton);
            sidebar.add(modeLabel);



            frame.add(sidebar, BorderLayout.SOUTH);
            frame.add(new JScrollPane(explanation), BorderLayout.EAST);
            frame.setVisible(true);
        });
    }

    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
}
