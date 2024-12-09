package org.example;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class DataGenerator {

    private static final String FILE_NAME = "wavelet_data.txt";
    private static final long FILE_SIZE = 500000000L;

    public static void main(String[] args) throws IOException {
        generateFile(FILE_NAME, FILE_SIZE);
    }

    public static void generateFile(String fileName, long size) throws IOException {
        Random random = new Random();
        String alphabet = "abcdefghijklmnopqrstuvwxyz";


        try(BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            for (int i = 0; i < size; i++) {
                writer.write(alphabet.charAt(random.nextInt(alphabet.length())));
            }
        }
        File file = new File(FILE_NAME);

        System.out.println("Файл успешно создан: " + fileName);
        System.out.println("Файл создан в: " + file.getAbsolutePath());
    }
}
