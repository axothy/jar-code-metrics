package org.itmo.lab1.util;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;
import org.objectweb.asm.tree.analysis.*;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;

public class ByteCodePrinter {

    private static String getUnqualifiedName(final String name) {
        var lastSlashIndex = name.lastIndexOf('/');
        if (lastSlashIndex == -1) {
            return name;
        } else {
            int endIndex = name.length();
            if (name.charAt(endIndex - 1) == ';') {
                endIndex--;
            }
            int lastBracketIndex = name.lastIndexOf('[');
            if (lastBracketIndex == -1) {
                return name.substring(lastSlashIndex + 1, endIndex);
            }
            return name.substring(0, lastBracketIndex + 1) + name.substring(lastSlashIndex + 1, endIndex);
        }
    }
    private static void analyzeMethod(
            final MethodNode method, final Analyzer<BasicValue> analyzer, final PrintWriter printWriter) {
        var textifier = new Textifier();
        var traceMethodVisitor = new TraceMethodVisitor(textifier);

        printWriter.println(method.name + method.desc);
        for (int i = 0; i < method.instructions.size(); ++i) {
            method.instructions.get(i).accept(traceMethodVisitor);

            var stringBuilder = new StringBuilder();
            var frame = analyzer.getFrames()[i];
            if (frame == null) {
                stringBuilder.append('?');
            } else {
                for (int j = 0; j < frame.getLocals(); ++j) {
                    stringBuilder.append(getUnqualifiedName(frame.getLocal(j).toString())).append(' ');
                }
                stringBuilder.append(" : ");
                for (int j = 0; j < frame.getStackSize(); ++j) {
                    stringBuilder.append(getUnqualifiedName(frame.getStack(j).toString())).append(' ');
                }
            }
            while (stringBuilder.length() < method.maxStack + method.maxLocals + 1) {
                stringBuilder.append(' ');
            }
            printWriter.print(Integer.toString(i + 100000).substring(1));
            printWriter.print(
                    " " + stringBuilder + " : " + textifier.text.get(textifier.text.size() - 1));
        }
        for (TryCatchBlockNode tryCatchBlock : method.tryCatchBlocks) {
            tryCatchBlock.accept(traceMethodVisitor);
            printWriter.print(" " + textifier.text.get(textifier.text.size() - 1));
        }
        printWriter.println();
    }

    public void printBytecode(ClassNode cn) {
        var sortMethod = cn.methods.get(1);
        var analyzer = new CheckFrameAnalyzer<>(new BasicVerifier());
        try {
            analyzer.analyze("dummy", sortMethod);
        } catch (AnalyzerException e) {
            throw new RuntimeException(e);
        }
        var pw = new PrintWriter(System.out);
        analyzeMethod(sortMethod, analyzer, pw);
        pw.flush();
    }

    public void printBubbleSortBytecode() throws IOException {
        var cn = new ClassNode();
        var classFileBytes = Files.readAllBytes(Path.of("build/classes/java/main/org/itmo/lab1/example/BubbleSort.class"));
        var classReader = new ClassReader(classFileBytes);
        classReader.accept(cn, ClassReader.EXPAND_FRAMES);
        printBytecode(cn);
    }
}
