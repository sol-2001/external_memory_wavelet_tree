package org;

import org.example.MappedCharSequence;
import org.example.WaveletTree;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
public class WaveletTreeTest {
    private static final String TEST_FILE_NAME = "test_data.txt";
    private static final String TEST_SEQUENCE = "abacaba";

    private WaveletTree waveletTree;

    @BeforeAll
    static void createTestFile() throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(TEST_FILE_NAME))) {
            writer.write(TEST_SEQUENCE);
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        MappedCharSequence seq = new MappedCharSequence(Path.of(TEST_FILE_NAME));
        waveletTree = new WaveletTree(seq);
    }

    @AfterAll
    static void cleanUp() {
        File file = new File(TEST_FILE_NAME);
        if (file.exists()) {
            file.delete();
        }
    }

    @Test
    void testAccess() throws Exception {
        assertEquals('a', waveletTree.access(0));
        assertEquals('b', waveletTree.access(1));
        assertEquals('a', waveletTree.access(2));
        assertEquals('c', waveletTree.access(3));
        assertEquals('a', waveletTree.access(4));
        assertEquals('b', waveletTree.access(5));
        assertEquals('a', waveletTree.access(6));
    }

    @Test
    void testRank() throws Exception {
        // Проверяем функцию rank(c, i)
        // Для "abacaba":
        // rank('a',0)=1; rank('a',1)=1; rank('a',2)=2; rank('a',3)=2; rank('a',4)=3; rank('a',5)=3; rank('a',6)=4
        // rank('b',0)=0; rank('b',1)=1; rank('b',5)=2
        // rank('c',3)=1

        assertEquals(1, waveletTree.rank('a', 0));
        assertEquals(1, waveletTree.rank('a', 1));
        assertEquals(2, waveletTree.rank('a', 2));
        assertEquals(2, waveletTree.rank('a', 3));
        assertEquals(3, waveletTree.rank('a', 4));
        assertEquals(3, waveletTree.rank('a', 5));
        assertEquals(4, waveletTree.rank('a', 6));

        assertEquals(0, waveletTree.rank('b', 0));
        assertEquals(1, waveletTree.rank('b', 1));
        assertEquals(2, waveletTree.rank('b', 5));

        assertEquals(1, waveletTree.rank('c', 3));
        assertEquals(1, waveletTree.rank('c', 6));
    }

    @Test
    void testBoundaries() throws Exception {
        // Проверяем поведение на границах
        // rank('a', 0) уже проверяли, но можно проверить на пустых префиксах

        assertThrows(Exception.class, () -> waveletTree.access(-1));
        assertThrows(Exception.class, () -> waveletTree.rank('a', -1));

        // Проверка индекса, равного длине последовательности
        // У нас длина = 7, значит max valid index = 6
        assertThrows(Exception.class, () -> waveletTree.access(7));
        assertThrows(Exception.class, () -> waveletTree.rank('a', 7));
    }

    @Test
    void testSingleCharSequence() throws Exception {
        // Проверим дерево на последовательности из одного символа (листочек)
        String single = "aaaaaa";
        File singleFile = new File("single.txt");
        try (BufferedWriter w = new BufferedWriter(new FileWriter(singleFile))) {
            w.write(single);
        }

        MappedCharSequence seq = new MappedCharSequence(Path.of("single.txt"));
        WaveletTree wt = new WaveletTree(seq);

        // Проверяем access
        for (int i = 0; i < single.length(); i++) {
            assertEquals('a', wt.access(i));
        }

        // Проверяем rank
        // rank('a',0)=1, rank('a',5)=6
        assertEquals(1, wt.rank('a', 0));
        assertEquals(6, wt.rank('a', 5));
        // rank('b', 5)=0, т.к. 'b' нет в последовательности
        assertEquals(0, wt.rank('b', 5));

        singleFile.delete();
    }

}
