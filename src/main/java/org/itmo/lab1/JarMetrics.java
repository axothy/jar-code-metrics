package org.itmo.lab1;

import org.itmo.lab1.util.Graph;
import org.itmo.lab1.util.Node;
import org.itmo.lab1.visitor.ClassAnalyzer;
import org.json.JSONObject;
import org.objectweb.asm.ClassReader;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JarMetrics {
    private int fields;
    private int classCount;
    private int methodsCount;
    private int aMetric, bMetric, cMetric;
    private final Graph graph;
    private final Map<String, Node> classMap;
    private final Node root;
    private final String jarFileName;

    public JarMetrics(String jarFileName) {
        this.jarFileName = jarFileName;
        root = new Node("java/lang/Object", "", new HashSet<>(), Arrays.asList("hashCode()",
                "equals(Ljava/lang/Object;)", "clone()", "toString()", "finalize()"));

        graph = new Graph();
        classMap = new HashMap<>();
        classMap.put(root.getClassName(), root);
    }

    public void getMetrics() throws IOException {
        try (JarFile sampleJar = new JarFile(jarFileName)) {
            Enumeration<JarEntry> enumeration = sampleJar.entries();

            while (enumeration.hasMoreElements()) {
                JarEntry entry = enumeration.nextElement();
                if (entry.getName().endsWith(".class")) {

                    ClassAnalyzer analyzer = new ClassAnalyzer();
                    ClassReader cr = new ClassReader(sampleJar.getInputStream(entry));
                    cr.accept(analyzer, 0);

                    classCount++;
                    fields += analyzer.getMetrics().getFieldCount();
                    aMetric += analyzer.getMetrics().getA();
                    bMetric += analyzer.getMetrics().getB();
                    cMetric += analyzer.getMetrics().getC();
                    methodsCount += analyzer.getMethodsCount();

                    Node node = new Node(analyzer.getClassName(), analyzer.getSuperName(),
                            analyzer.getInterfaces(), analyzer.getMethodSignatures());

                    classMap.put(node.getClassName(), node);
                }
            }
        }

        createGraph();
        graph.depthFirstTraversal(root, 1);

        int maxDepth = maxDepth();
        double averageDepth = averageDepth();
        int overridenMethods = overridenMethods();
        double averageOverridenMethods = (double) overridenMethods / (double) methodsCount;

        writeToJson(jarFileName, aMetric, bMetric, cMetric, (double) fields / classCount,
                maxDepth, averageDepth, averageOverridenMethods, methodsCount, overridenMethods);
    }

    private void createGraph() {
        for (Node from : classMap.values()) {
            Node to = classMap.get(from.getSuperclassName());
            //System.out.println(from + " -> " + to);
            graph.addEdge(from, to);
        }

        for (Node from : classMap.values()) {
            if (from.getClassName().equals(root.getClassName()))
                continue;

            for (String face : from.getInterfaces()) {
                Node to = classMap.get(face);
                graph.addEdge(from, to);
                //System.out.println(from + " -> " + to);
            }
        }
    }

    private int overridenMethods() {
        visitSuperClasses();
        visitInterfaces();

        return getOverridesForInterfaces() + getOverridesForSuperclasses();
    }

    private int maxDepth() {
        return graph.maxDepth;
    }

    private double averageDepth() {
        return graph.averageDepth();
    }

    /**
     * Count overriden methods for all nodes in the map.
     * Checks all parent-interfaces of every node in the map.
     */
    public int getOverridesForInterfaces() {
        int result = 0;

        for (Node node : classMap.values()) {                                //For every node
            if (node.getClassName().equals(root.getClassName()))             //skip Object class
                continue;

            for (String method : node.getMethods()) {                        //For every method in the node
                for (String face : node.getInterfaces()) {                   //For every parent-interface
                    Node parent = classMap.get(face);
                    if (parent != null && parent.getMethods().contains(method))
                        result++;
                }
            }
        }

        return result;
    }

    /**
     * Count overriden methods for all nodes in the map.
     * Checks all parent-superclasses of every node in the map.
     */
    public int getOverridesForSuperclasses() {
        int result = 0;
        for (Node node : classMap.values()) {                                //For every node
            if (node.getClassName().equals(root.getClassName()))             //skip Object class
                continue;

            for (String method : node.getMethods()) {                        //For every method in the node
                for (Node parent : node.getSuperClasses()) {                 //For every parent
                    if (parent != null && parent.getMethods().contains(method))
                        result++;
                }
            }
        }

        return result;
    }

    /**
     * Search all parent-superclasses for every node in the map
     */
    private void visitSuperClasses() {
        for (Node node : classMap.values()) {
            node.addSuperClass(classMap.get(node.getSuperclassName()));
            appendSuperclasses(node, node);
        }
    }

    /**
     * Add all parent-superclasses to node superclass list
     */
    private void appendSuperclasses(Node node, Node currNode) {
        Node superNode = classMap.get(currNode.getSuperclassName());
        if (superNode != null) {
            node.addSuperClass(superNode);
            appendSuperclasses(node, superNode);
        }
    }

    /**
     * Search all parent-interfaces for every node in the map
     */
    private void visitInterfaces() {
        for (Node node : classMap.values()) {
            appendInterfaces(node, node);
        }
    }


    private final Set<String> interfaces = new HashSet<>();

    /**
     * Retrieve all parent-interfaces for node
     */
    private void appendInterfaces(Node node, Node currNode) {
        for (String face : currNode.getInterfaces()) {
            Node faceNode = classMap.get(face);
            if (faceNode != null) {
                interfaces.addAll(faceNode.getInterfaces());
                appendInterfaces(node, faceNode);
            }
        }

        if (!interfaces.isEmpty()) {
            node.setInterfaces(interfaces);
        }
        interfaces.clear();
    }

    private static JSONObject writeToJson(String jarFileName, double aMetric, double bMetric, double cMetric,
                                          double fieldsAverage, double maxDepth, double averageDepth, double averageOverridenMethods,
                                          int methods, int overridenMethods) {
        JSONObject metricJson = new JSONObject();
        metricJson.put("jarName", jarFileName);
        metricJson.put("fieldsAverage", fieldsAverage);
        metricJson.put("A", aMetric);
        metricJson.put("B", bMetric);
        metricJson.put("C", cMetric);
        metricJson.put("ABC", Math.sqrt(aMetric * aMetric + bMetric * bMetric + cMetric * cMetric));
        metricJson.put("maxInheritanceDepth", maxDepth);
        metricJson.put("averageInheritanceDepth", averageDepth);
        metricJson.put("averageOverridenMethods", averageOverridenMethods);
        metricJson.put("methods", methods);
        metricJson.put("overridenMethods", overridenMethods);

        try {
            File file = new File("metrics.json");
            file.createNewFile();
            FileWriter fileWriter = new FileWriter(file);
            System.out.print(metricJson);

            fileWriter.write(metricJson.toString(4));
            fileWriter.flush();
            fileWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return metricJson;
    }

    public static void main(String[] args) throws IOException {
        JarMetrics metrics = new JarMetrics("/Users/axothy/.gradle/caches/modules-2/files-2.1/org.springframework/spring-web/5.3.8/daa288e67b0f2e09a033500d5ce8406677c5045c/spring-web-5.3.8.jar");
        metrics.getMetrics();
    }
}