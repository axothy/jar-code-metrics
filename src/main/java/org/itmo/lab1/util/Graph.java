package org.itmo.lab1.util;

import java.util.*;

public class Graph {
    private Map<Node, List<Node>> adjacencyList;

    public Graph() {
        adjacencyList = new HashMap<>();
    }

    public void addNode(Node node) {
        adjacencyList.putIfAbsent(node, new ArrayList<>());
    }


    public void addEdge(Node node1, Node node2) {
        if (!adjacencyList.containsKey(node1))
            addNode(node1);

        if (!adjacencyList.containsKey(node2))
            addNode(node2);

        //adjacencyList.get(node1).add(node2);
        adjacencyList.get(node2).add(node1);
    }

    public int maxDepth = -1;
    public final Set<Node> visited = new LinkedHashSet<Node>();
    private Map<String, Integer> depths = new HashMap<>();

    public double averageDepth() {
        return depths.entrySet().stream().mapToInt(entry -> entry.getValue()).average().orElse(Double.NaN);
    }

    public int depthFirstTraversal(Node root, int count) {
        visited.add(root);

        for (Node node : adjacencyList.get(root)) {
            if (!visited.contains(node)) {
                int dfs = depthFirstTraversal(node, ++count);
                if (maxDepth < dfs) {
                    maxDepth = dfs;
                }

                depths.put(node.getClassName(), count);
                count--;
            }
        }

        return count;
    }

    public Set<Node> getVisited() {
        return visited;
    }
}
