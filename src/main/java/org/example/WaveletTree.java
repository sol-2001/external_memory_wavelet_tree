package org.example;

import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class WaveletTree {
    private static final String FILE_NAME = "wavelets.txt";
    private static final int BUFFER_SIZE = 1024 * 1024 * 512;
    private static final int BATCH_SIZE = 1024;
    private final Node root;
    private final MappedByteBuffer buffer;
    private final byte[] batchBuffer = new byte[BATCH_SIZE];
    private int batchIndex = 0;

    private static class Node {
        int min, max;
        int bitMapOffset;
        int bitMapSize;
        Node left, right;

        Node(int min, int max) {
            this.min = min;
            this.max = max;
        }
    }

    public WaveletTree(CharSequenceReader reader) throws Exception {
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;

        for (int i = 0; i < reader.length(); i++) {
            int c = reader.charAt(i);
            if (c < min) min = c;
            if (c > max) max = c;
        }

        try (RandomAccessFile file = new RandomAccessFile(FILE_NAME, "rw")) {
            file.setLength(BUFFER_SIZE);
            buffer = file.getChannel().map(FileChannel.MapMode.READ_WRITE, 0, BUFFER_SIZE);
        }

        root = buildTree(reader, min, max);
        flushBatch();
    }

    private Node buildTree(CharSequenceReader reader, int min, int max) throws Exception {
        int size = reader.length();

        if (min == max) {
            Node node = new Node(min, max);
            node.bitMapSize = size;
            return node;
        }

        int mid = (min + max) / 2;
        Node node = new Node(min, max);

        int countLeft = 0;
        for (int i = 0; i < size; i++) {
            int c = reader.charAt(i);
            if (c <= mid) countLeft++;
        }
        int countRight = size - countLeft;

        char[] leftArr = new char[countLeft];
        char[] rightArr = new char[countRight];

        byte[] bitMap = new byte[(size + 7) / 8];

        int leftPos = 0;
        int rightPos = 0;
        for (int i = 0; i < size; i++) {
            int c = reader.charAt(i);
            if (c <= mid) {
                leftArr[leftPos++] = (char) c;
                setBit(bitMap, i, false);
            } else {
                rightArr[rightPos++] = (char) c;
                setBit(bitMap, i, true);
            }
        }

        node.bitMapOffset = saveBitMap(bitMap);
        node.bitMapSize = size;

        if (countLeft > 0) {
            node.left = buildTree(new ArrayCharSequence(leftArr), min, mid);
        }
        if (countRight > 0) {
            node.right = buildTree(new ArrayCharSequence(rightArr), mid + 1, max);
        }

        return node;
    }

    private int saveBitMap(byte[] bitMap) {
        int offset = buffer.position();
        for (byte b : bitMap) {
            addToBatch(b);
        }
        flushBatch();
        return offset;
    }

    private void addToBatch(byte value) {
        batchBuffer[batchIndex++] = value;
        if (batchIndex == BATCH_SIZE) {
            flushBatch();
        }
    }

    private void flushBatch() {
        if (batchIndex > 0) {
            buffer.put(batchBuffer, 0, batchIndex);
            batchIndex = 0;
        }
    }

    private byte[] loadBitMapBatch(int offset, int length) {
        flushBatch();
        byte[] batch = new byte[length];
        buffer.position(offset);
        buffer.get(batch, 0, length);
        return batch;
    }

    private void setBit(byte[] bitMap, int index, boolean value) {
        int byteIndex = index / 8;
        int bitIndex = index % 8;

        if (value) {
            bitMap[byteIndex] |= (byte) (1 << bitIndex);
        } else {
            bitMap[byteIndex] &= (byte) ~(1 << bitIndex);
        }
    }

    private boolean getBit(byte[] bitMap, int index) {
        int byteIndex = index / 8;
        int bitIndex = index % 8;
        return ((bitMap[byteIndex] >> bitIndex) & 1) != 0;
    }

    public char access(int index) throws Exception {
        if (index < 0 || index >= root.bitMapSize) {
            throw new IndexOutOfBoundsException("Index " + index + " out of range [0, " + (root.bitMapSize - 1) + "]");
        }
        return (char) accessHelper(root, index);
    }

    private int accessHelper(Node node, int index) {
        if (node.min == node.max) {
            return node.min;
        }

        byte[] bitMap = loadBitMapBatch(node.bitMapOffset, (node.bitMapSize + 7) / 8);
        boolean bit = getBit(bitMap, index);
        int rank = rankInBitmap(node.bitMapOffset, node.bitMapSize, bit, index);

        if (!bit) {
            return accessHelper(node.left, rank - 1);
        } else {
            return accessHelper(node.right, rank - 1);
        }
    }


    public int rank(char character, int index) throws Exception {
        if (index < 0 || index >= root.bitMapSize) {
            throw new IndexOutOfBoundsException("Index " + index + " out of range [0, " + (root.bitMapSize - 1) + "]");
        }
        return rankHelper(root, character, index);
    }

    private int rankHelper(Node node, int target, int index) throws Exception {
        if (target < node.min || target > node.max) {
            return 0;
        }

        if (node.min == node.max) {
            return (target == node.min) ? index + 1 : 0;
        }

        boolean bit = target > node.left.max;
        int rank = rankInBitmap(node.bitMapOffset, node.bitMapSize, bit, index);

        if (!bit) {
            return rankHelper(node.left, target, rank - 1);
        } else {
            return rankHelper(node.right, target, rank - 1);
        }
    }

    private int rankInBitmap(int offset, int size, boolean bit, int index) {
        byte[] bitMap = loadBitMapBatch(offset, (size + 7) / 8);

        int count = 0;
        for (int i = 0; i <= index && i < size; i++) {
            boolean b = getBit(bitMap, i);
            if (b == bit) {
                count++;
            }
        }
        return count;
    }
}
