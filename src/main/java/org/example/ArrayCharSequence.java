package org.example;

public class ArrayCharSequence implements CharSequenceReader {
    private char[] data;

    public ArrayCharSequence(char[] data) {
        this.data = data;
    }

    @Override
    public int length() {
        return data.length;
    }

    @Override
    public char charAt(int index) {
        return data[index];
    }
}
