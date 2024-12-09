package org.example;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class MappedCharSequence implements CharSequenceReader {
    private final MappedByteBuffer buffer;
    private final int length;

    public MappedCharSequence(Path path) throws IOException {
        try(var channel = FileChannel.open(path, StandardOpenOption.READ)) {
            long fileSize = channel.size();
            if (fileSize > Integer.MAX_VALUE) {
                throw new RuntimeException("File is too large");
            }
            this.length = (int) fileSize;
            this.buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, length);
        }
    }

    @Override
    public char charAt(int index) {
        return (char) (buffer.get(index) & 0xFF);
    }

    @Override
    public int length() {
        return length;
    }

}
