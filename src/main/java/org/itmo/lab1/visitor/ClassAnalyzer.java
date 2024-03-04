package org.itmo.lab1.visitor;

import org.itmo.lab1.util.Metrics;
import org.objectweb.asm.*;

import java.util.*;

import static org.objectweb.asm.Opcodes.*;

public class ClassAnalyzer extends ClassVisitor {
    private Metrics metrics;
    private String className;
    private String superName;
    private boolean isInterface;
    private List<String> methodSignatures;
    private Set<String> interfaces;
    private int methodsCount;

    public ClassAnalyzer() {
        super(ASM8);
        metrics = new Metrics();
        methodSignatures = new ArrayList<>();
        methodsCount = 0;
    }

    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        this.isInterface = (access & ACC_INTERFACE) != 0;
        this.className = name;
        this.superName = superName;
        this.interfaces = new HashSet<>(Arrays.asList(interfaces));

    }

    public void visitSource(String source, String debug) {
    }

    public void visitOuterClass(String owner, String name, String desc) {
    }

    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        return null;
    }

    public void visitAttribute(Attribute attr) {
    }

    public void visitInnerClass(String name, String outerName, String innerName, int access) {
    }

    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        metrics.increaseFields();
        return null;
    }

    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        methodsCount++;
        String methodSignature = name + retrieveArgs(desc);

        if (!methodSignature.contains("<init>"))
            methodSignatures.add(methodSignature);

        return new ABCMetricsAnalyzer(metrics);
    }

    public void visitEnd() {
    }

    public Metrics getMetrics() {
        return metrics;
    }

    public boolean isInterface() {
        return isInterface;
    }
    private String retrieveArgs(String desc) {
        int startIndex = desc.indexOf("(");
        int endIndex = desc.indexOf(")");

        String result = desc.substring(startIndex, endIndex + 1);

        return result;
    }
    public List<String> getMethodSignatures() {
        return methodSignatures;
    }
    public String getClassName() {
        return className;
    }

    public String getSuperName() {
        return superName;
    }

    public Set<String> getInterfaces() {
        return interfaces;
    }

    public int getMethodsCount() {
        return methodsCount;
    }
}

