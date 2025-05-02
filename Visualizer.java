import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

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
    private boolean isWeighted = false;
    private boolean quizMode = false;

    
    // Constructor for data structures and mouse listener.

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

        // Draw edges first
        g.setColor(Color.BLACK);
        for (int[] edge : edges) {
            Point p1 = nodes.get(edge[0]);
            Point p2 = nodes.get(edge[1]);
            drawEdge(g, p1, p2, isDirected);
            if (isWeighted) {
                int u = edge[0];
                int v = edge[1];
                int w = graph.getWeight(u, v);
                int midX = (p1.x + p2.x) / 2;
                int midY = (p1.y + p2.y) / 2;
                double dx = p2.x - p1.x;
                double dy = p2.y - p1.y;
                double dist = Math.hypot(dx, dy);
                if (dist != 0) {
                    double px = -dy / dist;
                    double py = dx / dist;
                    int labelOffset = 15;
                    midX += (int) (px * labelOffset);
                    midY += (int) (py * labelOffset);
                }
                g.setColor(Color.RED);
                g.drawString(String.valueOf(w), midX, midY);
                g.setColor(Color.BLACK);
            }
        }

        // Draw nodes on top of edges
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

    
    // Draws an edge with directed arrows for directed graphs.
    private void drawEdge(Graphics g, Point p1, Point p2, boolean directed) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setStroke(new BasicStroke(2));
        double dx = p2.x - p1.x;
        double dy = p2.y - p1.y;
        double dist = Math.hypot(dx, dy);
        if (dist == 0) {
            return;
        }
        int startX = (int) (p1.x + dx * radius / dist);
        int startY = (int) (p1.y + dy * radius / dist);
        int endX = (int) (p2.x - dx * radius / dist);
        int endY = (int) (p2.y - dy * radius / dist);
        g2.drawLine(startX, startY, endX, endY);

        if (directed) {
            double theta = Math.atan2(endY - startY, endX - startX);
            double phi = Math.toRadians(30);
            int barb = 10;
            for (int j = 0; j < 2; j++) {
                double angle;
                if (j == 0) {
                    angle = theta + phi;
                } else {
                    angle = theta - phi;
                }
                int x = (int) (endX - barb * Math.cos(angle));
                int y = (int) (endY - barb * Math.sin(angle));
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
                    int w = 1;
                    if (isWeighted) {
                        w = -1;
                        while (w < 0) {
                            String ws = JOptionPane.showInputDialog(
                                this,
                                "Enter non-negative weight for edge " + selectedNode + " → " + nodeIdx + ":",
                                "Edge Weight",
                                JOptionPane.QUESTION_MESSAGE
                            );
                            if (ws == null) {
                                w = 1;
                                break;
                            }
                            try {
                                w = Integer.parseInt(ws.trim());
                                if (w < 0) {
                                    JOptionPane.showMessageDialog(
                                        this,
                                        "Weight must be 0 or positive. Please try again."
                                    );
                                }
                            } catch (NumberFormatException ex) {
                                JOptionPane.showMessageDialog(
                                    this,
                                    "Invalid input. Please enter a non-negative integer."
                                );
                                w = -1;
                            }
                        }
                    }
                    graph.addEdge(selectedNode, nodeIdx, w);
                    if (!isDirected) {
                        graph.addEdge(nodeIdx, selectedNode, w);
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
            if (nodes.get(i).distance(p) <= radius) {
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

    public Graph getGraph() {
        return dummyGraph();
    }

    public Graph dummyGraph() {
        Graph dummy = new Graph(nodes.size());
        for (int[] e : edges) {
            dummy.addEdge(e[0], e[1], 1);
            if (!isDirected) {
                dummy.addEdge(e[1], e[0], 1);
            }
        }
        return dummy;
    }

    public void toggleDirected() {
        // Preserve existing weights
        Map<String, Integer> oldWeights = new HashMap<>();
        for (int[] edge : edges) {
            String key = edge[0] + "," + edge[1];
            oldWeights.put(key, graph.getWeight(edge[0], edge[1]));
        }
        graph.clear();
        isDirected = !isDirected;
        // Reinsert edges
        for (int[] edge : edges) {
            int u = edge[0];
            int v = edge[1];
            int w = oldWeights.get(u + "," + v);
            graph.addEdge(u, v, w);
            if (!isDirected) {
                graph.addEdge(v, u, w);
            }
        }
        repaint();
    }

    public void toggleWeighted() {
        isWeighted = !isWeighted;
        if (isWeighted) {
            for (int[] edge : edges) {
                int u = edge[0];
                int v = edge[1];
                int w = -1;
                while (w < 0) {
                    String wStr = JOptionPane.showInputDialog(
                        this,
                        "Enter non-negative weight for edge " + u + " → " + v + ":",
                        "Edge Weight",
                        JOptionPane.QUESTION_MESSAGE
                    );
                    if (wStr == null) {
                        w = 1;
                        break;
                    }
                    try {
                        w = Integer.parseInt(wStr.trim());
                        if (w < 0) {
                            JOptionPane.showMessageDialog(
                                this,
                                "Weight must be 0 or positive. Please try again."
                            );
                        }
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(
                            this,
                            "Invalid input. Please enter a non-negative integer."
                        );
                        w = -1;
                    }
                }
                graph.setWeight(u, v, w);
                if (!isDirected) {
                    graph.setWeight(v, u, w);
                }
            }
        } else {
            for (int[] edge : edges) {
                int u = edge[0];
                int v = edge[1];
                graph.setWeight(u, v, 1);
                if (!isDirected) {
                    graph.setWeight(v, u, 1);
                }
            }
        }
        repaint();
    }

    public void resetAll() {
        nodes.clear();
        edges.clear();
        fullPath.clear();
        highlightedPath.clear();
        graph.clear();
        selectedNode = -1;
        highlightIndex = -1;
        repaint();
    }

    private List<Integer> parseAnswer(String input) {
        List<Integer> list = new ArrayList<>();
        if (input == null) {
            return list;
        }
        for (String tok : input.split(",")) {
            list.add(Integer.parseInt(tok.trim()));
        }
        return list;
    }

    private void giveFeedback(List<Integer> user, List<Integer> correct) {
        if (user.equals(correct)) {
            JOptionPane.showMessageDialog(this, "Correct!");
        } else {
            JOptionPane.showMessageDialog(
                this,
                "Not quite.\nYour answer:    " + user + "\nCorrect answer: " + correct
            );
        }
    }

    private void startBFSQuiz() {
        try {
            String in = JOptionPane.showInputDialog(this, "Quiz BFS: enter start node:");
            int start = Integer.parseInt(in.trim());
            List<Integer> correct = Algo.bfs(dummyGraph(), start);
            String ans = JOptionPane.showInputDialog(
                this, "Enter your BFS order (comma-separated):"
            );
            List<Integer> user = parseAnswer(ans);
            giveFeedback(user, correct);
            visualize(correct);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid input. Aborting quiz.");
        }
    }

    private void startDFSQuiz() {
        try {
            String in = JOptionPane.showInputDialog(this, "Quiz DFS: enter start node:");
            int start = Integer.parseInt(in.trim());
            List<Integer> correct = Algo.dfs(dummyGraph(), start);
            String ans = JOptionPane.showInputDialog(
                this, "Enter your DFS order (comma-separated):"
            );
            List<Integer> user = parseAnswer(ans);
            giveFeedback(user, correct);
            visualize(correct);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid input. Aborting quiz.");
        }
    }

    private void startDijkstraQuiz() {
        try {
            String s1 = JOptionPane.showInputDialog(this, "Quiz Dijkstra: source node:");
            int src = Integer.parseInt(s1.trim());
            String s2 = JOptionPane.showInputDialog(this, "Destination node:");
            int dst = Integer.parseInt(s2.trim());
            List<Integer> correct = Algo.dijkstra(dummyGraph(), src, dst);
            String ans = JOptionPane.showInputDialog(
                this, "Enter shortest path (comma-separated):"
            );
            List<Integer> user = parseAnswer(ans);
            giveFeedback(user, correct);
            visualize(correct);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid input. Aborting quiz.");
        }
    }

    private void startTopoQuiz() {
        List<Integer> correct = Algo.toposort(dummyGraph());
        String ans = JOptionPane.showInputDialog(
            this, "Enter your topological sort order (comma-separated):"
        );
        List<Integer> user = parseAnswer(ans);
        giveFeedback(user, correct);
        visualize(correct);
    }

    // Main for UI and  actions.
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
            JButton toggleButton = new JButton("Toggle Directed");
            JButton weightToggle = new JButton("Toggle Weighted");
            JButton resetButton = new JButton("Reset All");
            JButton quizToggle = new JButton("Start Quiz");

            JLabel modeLabel = new JLabel();
            updateModeLabel(modeLabel, panel);

            // Algorithm button listeners
            bfsButton.addActionListener(e -> {
                if (!panel.quizMode) {
                    String input = JOptionPane.showInputDialog("Start node for BFS:");
                    int start = Integer.parseInt(input.trim());
                    List<Integer> order = Algo.bfs(panel.getGraph(), start);
                    panel.setPath(order);
                } else {
                    panel.startBFSQuiz();
                }
            });
            dfsButton.addActionListener(e -> {
                if (!panel.quizMode) {
                    String input = JOptionPane.showInputDialog("Start node for DFS:");
                    int start = Integer.parseInt(input.trim());
                    List<Integer> order = Algo.dfs(panel.getGraph(), start);
                    panel.setPath(order);
                } else {
                    panel.startDFSQuiz();
                }
            });
            dijkstraButton.addActionListener(e -> {
                if (!panel.quizMode) {
                    String s1 = JOptionPane.showInputDialog("Source node for Dijkstra:");
                    int src = Integer.parseInt(s1.trim());
                    String s2 = JOptionPane.showInputDialog("Destination node for Dijkstra:");
                    int dst = Integer.parseInt(s2.trim());
                    List<Integer> path = Algo.dijkstra(panel.getGraph(), src, dst);
                    panel.setPath(path);
                } else {
                    panel.startDijkstraQuiz();
                }
            });
            topoButton.addActionListener(e -> {
                if (!panel.quizMode) {
                    List<Integer> order = Algo.toposort(panel.getGraph());
                    if (order == null) {
                        JOptionPane.showMessageDialog(panel, "Graph is not a DAG.");
                    } else {
                        panel.setPath(order);
                    }
                } else {
                    panel.startTopoQuiz();
                }
            });

            // Navigation actions
            prevButton.addActionListener(e -> panel.prevStep());
            nextButton.addActionListener(e -> panel.nextStep());

            // Toggle directed/undirected
            toggleButton.addActionListener(e -> {
                panel.toggleDirected();
                String mode = (panel.isDirected ? "DIRECTED" : "UNDIRECTED") +
                              " / " +
                              (panel.isWeighted ? "WEIGHTED" : "UNWEIGHTED");
                JOptionPane.showMessageDialog(panel, "Graph is now " + mode + "!");
                updateModeLabel(modeLabel, panel);
            });

            // Toggle weighted/unweighted
            weightToggle.addActionListener(e -> {
                panel.toggleWeighted();
                String mode = (panel.isDirected ? "DIRECTED" : "UNDIRECTED") +
                              " / " +
                              (panel.isWeighted ? "WEIGHTED" : "UNWEIGHTED");
                JOptionPane.showMessageDialog(panel, "Graph is now " + mode + "!");
                updateModeLabel(modeLabel, panel);
            });

            // Reset
            resetButton.addActionListener(e -> panel.resetAll());

            // Quiz mode toggle
            quizToggle.addActionListener(e -> {
                panel.quizMode = !panel.quizMode;
                quizToggle.setText(panel.quizMode ? "End Quiz" : "Start Quiz");
                updateModeLabel(modeLabel, panel);
            });

            // Instruction text
            JTextArea explanation = new JTextArea(8, 20);
            explanation.setEditable(false);
            explanation.setLineWrap(true);
            explanation.setWrapStyleWord(true);
            explanation.setText(
                "Instructions:\n\n" +
                "• Click to add nodes.\n" +
                "• Click two nodes to connect them.\n" +
                "• Toggle directed/undirected anytime.\n" +
                "• Toggle weighted/unweighted anytime.\n" +
                "• Reset to start over.\n" +
                "• Use Next Step and Prev Step to step through the current algorithm’s path visualization." +
                "• Start Quiz to test yourself on BFS, DFS, Dijkstra, or Topological Sort. (Note: BFS/DFS follows class behavior, continuing from a random unvisited node after finishing all reachable nodes from the starting node, until all nodes are traversed.)\n" + 
                "• Please run in fullscreen to ensure no buttons are clipped."

            );

            // Sidebar
            sidebar.add(weightToggle);
            sidebar.add(bfsButton);
            sidebar.add(dfsButton);
            sidebar.add(dijkstraButton);
            sidebar.add(topoButton);
            sidebar.add(prevButton);
            sidebar.add(nextButton);
            sidebar.add(toggleButton);
            sidebar.add(resetButton);
            sidebar.add(quizToggle);
            sidebar.add(modeLabel);

            frame.add(sidebar, BorderLayout.SOUTH);
            frame.add(new JScrollPane(explanation), BorderLayout.EAST);
            frame.setVisible(true);
        });
    }

    // Helper to update the mode label text based.

    private static void updateModeLabel(JLabel label, Visualizer panel) {
        if (panel.quizMode) {
            label.setText("Current Mode: QUIZ");
        } else {
            String text = (panel.isDirected ? "DIRECTED" : "UNDIRECTED") +
                          " / " +
                          (panel.isWeighted ? "WEIGHTED" : "UNWEIGHTED");
            label.setText("Current Mode: " + text);
        }
    }

    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
}
