package org.itmo.lab1.visitor;

import org.itmo.lab1.util.Metrics;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.*;

public class ABCMetricsAnalyzer extends MethodVisitor {
    private Metrics metrics;

    public ABCMetricsAnalyzer(Metrics metrics) {
        super(ASM8);
        this.metrics = metrics;
    }

    /**
     * Assigment count.Checks local variables.
     * Adds one to the assignment count for each occurrence of an assignment operator,
     * excluding constant declarations:
     * =  *=  /=  %=  +=  <<=  >>=  &=  |=  ^=  >>>=
     *
     * @param opcode   the opcode of the local variable instruction to be visited. This opcode is either
     *                 ILOAD, LLOAD, FLOAD, DLOAD, ALOAD, ISTORE, LSTORE, FSTORE, DSTORE, ASTORE or RET.
     * @param varIndex the operand of the instruction to be visited. This operand is the index of a
     *                 local variable.
     */
    @Override
    public void visitVarInsn(int opcode, int varIndex) {
        switch (opcode) {
            case Opcodes.ISTORE, Opcodes.LSTORE, Opcodes.FSTORE, Opcodes.DSTORE, Opcodes.ASTORE -> metrics.increaseA();
        }

        super.visitVarInsn(opcode, varIndex);
    }

    /**
     * Branch count.
     * Adds one to the branch count for each function call or class method call.
     * Adds one to the branch count for each occurrence of the new operator.
     *
     * @param opcode      the opcode of the type instruction to be visited. This opcode is either
     *                    INVOKEVIRTUAL, INVOKESPECIAL, INVOKESTATIC or INVOKEINTERFACE.
     * @param owner       the internal name of the method's owner class (see {@link
     *                    Type#getInternalName()}).
     * @param name        the method's name.
     * @param descriptor  the method's descriptor (see {@link Type}).
     * @param isInterface if the method's owner class is an interface.
     */
    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        metrics.increaseB();

        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
    }

    /**
     * Branch count.
     * Adds one to the branch count for each occurrence of the new operator.
     *
     * @param opcode the opcode of the type instruction to be visited. This opcode is either NEW,
     *               ANEWARRAY, CHECKCAST or INSTANCEOF.
     * @param type   the operand of the instruction to be visited. This operand must be the internal
     *               name of an object or array class (see {@link Type#getInternalName()}).
     */
    @Override
    public void visitTypeInsn(int opcode, String type) {
        switch (opcode) {
            case Opcodes.NEW, Opcodes.NEWARRAY, Opcodes.ANEWARRAY, Opcodes.MULTIANEWARRAY -> metrics.increaseB();
        }

        super.visitTypeInsn(opcode, type);
    }

    /**
     * Branch count.
     * Adds one to the branch count for each occurrence of the new operator.
     *
     * @param opcode  the opcode of the instruction to be visited. This opcode is either BIPUSH, SIPUSH
     *                or NEWARRAY.
     * @param operand the operand of the instruction to be visited.<br>
     *                When opcode is BIPUSH, operand value should be between Byte.MIN_VALUE and Byte.MAX_VALUE.
     *                <br>
     *                When opcode is SIPUSH, operand value should be between Short.MIN_VALUE and Short.MAX_VALUE.
     *                <br>
     *                When opcode is NEWARRAY, operand value should be one of {@link Opcodes#T_BOOLEAN}, {@link
     *                Opcodes#T_CHAR}, {@link Opcodes#T_FLOAT}, {@link Opcodes#T_DOUBLE}, {@link Opcodes#T_BYTE},
     *                {@link Opcodes#T_SHORT}, {@link Opcodes#T_INT} or {@link Opcodes#T_LONG}.
     */
    @Override
    public void visitIntInsn(int opcode, int operand) {
        switch (opcode) {
            case Opcodes.NEW, Opcodes.NEWARRAY, Opcodes.ANEWARRAY, Opcodes.MULTIANEWARRAY -> metrics.increaseB();
        }

        super.visitIntInsn(opcode, operand);
    }

    /**
     * Condition count.
     * Adds one to the condition count for each use of a conditional operator:
     * ==  !=  <=  >=  <  >
     *
     * @param opcode the opcode of the type instruction to be visited. This opcode is either IFEQ,
     *               IFNE, IFLT, IFGE, IFGT, IFLE, IF_ICMPEQ, IF_ICMPNE, IF_ICMPLT, IF_ICMPGE, IF_ICMPGT,
     *               IF_ICMPLE, IF_ACMPEQ, IF_ACMPNE, GOTO, JSR, IFNULL or IFNONNULL.
     * @param label  the operand of the instruction to be visited. This operand is a label that
     *               designates the instruction to which the jump instruction may jump.
     */
    @Override
    public void visitJumpInsn(int opcode, Label label) {
        metrics.increaseC();

        super.visitJumpInsn(opcode, label);
    }

    /**
     * Condition count. Adds one to the condition count for each use of the try-catch keyword
     *
     * @param start   the beginning of the exception handler's scope (inclusive).
     * @param end     the end of the exception handler's scope (exclusive).
     * @param handler the beginning of the exception handler's code.
     * @param type    the internal name of the type of exceptions handled by the handler (see {@link
     *                Type#getInternalName()}), or {@literal null} to catch any exceptions (for "finally"
     *                blocks).
     */
    @Override
    public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
        metrics.increaseC();

        super.visitTryCatchBlock(start, end, handler, type);
    }

    /**
     * Condition count.
     *
     * @param dflt   beginning of the default handler block.
     * @param keys   the values of the keys.
     * @param labels beginnings of the handler blocks. {@code labels[i]} is the beginning of the
     *               handler block for the {@code keys[i]} key.
     */
    @Override
    public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
        for (int ignored : keys)
            metrics.increaseC();

        super.visitLookupSwitchInsn(dflt, keys, labels);
    }

    public Metrics getMetrics() {
        return metrics;
    }
}

