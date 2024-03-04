package org.itmo.lab1.util;

public class Metrics {
    private int A; //Assigment count
    private int B; //Branch count
    private int C; //Condition count
    private int fieldCount;

    public Metrics() {
        A = 0;
        B = 0;
        C = 0;
        fieldCount = 0;
    }
    public int getA() {
        return A;
    }
    public int getB() {
        return B;
    }
    public int getC() {
        return C;
    }
    public int getFieldCount() {
        return fieldCount;
    }
    public void increaseFields() {
        fieldCount++;
    }
    public void increaseA() {
        A++;
    }
    public void increaseB() {
        B++;
    }
    public void increaseC() {
        C++;
    }
}
