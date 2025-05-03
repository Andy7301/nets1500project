# nets1500project

Interactive Graph Algorithm Visualizer

Description:

This Java Swing application lets users build and explore graphs interactively. You can click to add nodes, draw directed or undirected edges, and run four fundamental graph algorithms, BFS, DFS, Dijkstra’s, and Topological Sort, and execute each algorithm step by step. A built in quiz mode prompts you to predict the algorithm’s output before revealing and animating the correct result. This teaching aid deepens understanding of graph structures and algorithm behavior.

Category: Graph and graph algorithms

Work Breakdown:

Andy Wang – Implemented BFS, DFS, and Dijkstra’s algorithms, implemented Graph.java, added forward/backward step controls, created the instruction panel, created the README and user manual, wrote comments, handled negative weight and quiz errors.

Edward Zhang – Built the visualizer UI for node placement and edge creation, connected algorithm buttons, implemented directed/undirected and weighted/unweighted toggles, added reset functionality.

Jereth Liu – Implemented Topo Sort with cycle detection, developed the quiz mode, helped preserve graph structure when toggling between weighted/unweighted, modified BFS/DFS to traverse disconnected components like how they were taught in class.