package org.itmo.lab1.util;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Node {
    private final String className;
    private String superclassName;
    private Set<String> interfaces;
    private List<String> methods;
    private Set<Node> superClasses;

    public Node(String key, String superclassName, Set<String> interfaces, List<String> methods) {
        this.className = key;
        this.superclassName = superclassName;
        this.interfaces = interfaces;
        this.methods = methods;
        this.superClasses = new HashSet<>();
    }

    public String getClassName() {
        return className;
    }

    public String getSuperclassName() {
        return superclassName;
    }

    public List<String> getMethods() {
        return methods;
    }

    public Set<String> getInterfaces() {
        return interfaces;
    }

    public void addSuperClass(Node superClass) {
        superClasses.add(superClass);
    }

    public Set<Node> getSuperClasses() {
        return superClasses;
    }

    public void setInterfaces(Set<String> interfaces) {
        this.interfaces = interfaces;
    }

    @Override
    public String toString() {
        return className;
    }
}

