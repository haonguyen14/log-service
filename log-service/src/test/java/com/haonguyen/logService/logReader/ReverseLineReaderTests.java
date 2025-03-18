package com.haonguyen.logService.logReader;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class ReverseLineReaderTests {
    @TempDir
    static File tempDir;

    @Test
    public void readLine_fileNotEnd_returnAtLineBreak() throws IOException {
        File file = createFile("fileA", "line1\nline2\nline3");
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r")) {
            Optional<String> line = ReverseLineReader.readLine(randomAccessFile, randomAccessFile.length()-1);
            assertEquals("line3", line.get());
        }
    }

    @Test
    public void readLine_fileNotEnd_returnAtEOF() throws IOException {
        File file = createFile("fileB", "line1");
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r")) {
            Optional<String> line = ReverseLineReader.readLine(randomAccessFile, randomAccessFile.length()-1);
            assertEquals("line1", line.get());
        }
    }

    @Test
    public void readLine_emptyFile_returnEmptyLine() throws IOException {
        File file = createFile("fileC", "");
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r")) {
            Optional<String> line = ReverseLineReader.readLine(randomAccessFile, randomAccessFile.length()-1);
            assertTrue(line.isEmpty());
        }
    }

    @Test
    public void readNLines_readMultipleLines() throws IOException {
        File file = createFile("fileD", "\nline1\nline2\n\nline3");

        try (RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r")) {
            Lines lines = ReverseLineReader.
                    readNLines(randomAccessFile, randomAccessFile.length()-1, 4);
            assertEquals(lines.getLines(), Arrays.asList("line3", "", "line2", "line1"));
        }
    }

    @Test
    public void readNLines_moreLinesThanN_readMultipleLines() throws IOException {
        File file = createFile("fileE", "\nline1\nline2\n\nline3");

        try (RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r")) {
            Lines lines = ReverseLineReader.
                    readNLines(randomAccessFile, randomAccessFile.length()-1, 2);
            assertEquals(lines.getLines(), Arrays.asList("line3", ""));
            assertEquals(lines.getPtr(), randomAccessFile.length()-8);
        }
    }

    @Test
    public void readNLines_lessLinesThanN_readMultipleLines() throws IOException {
        File file = createFile("fileF", "\nline1\nline2\nline3");

        try (RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r")) {
            Lines lines = ReverseLineReader.
                    readNLines(randomAccessFile, randomAccessFile.length()-1, 1000);
            assertEquals(lines.getLines(), Arrays.asList("line3", "line2", "line1"));
            assertEquals(lines.getPtr(), -1);
        }
    }

    @Test
    public void snapOnNewline_inMiddle_snapOnFarLeftNewline() throws IOException {
        File file = createFile("snapOnNewLineA", "line1\nline2aaaaaaaaa\nline3");

        try (RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r")) {
            assertEquals(5, ReverseLineReader.snapOnNewline(randomAccessFile, 9));
        }
    }

    @Test
    public void snapOnNewline_atNewline_snapOnCurrentPtr() throws IOException {
        File file = createFile("snapOnNewLineB", "line1\nline2aaaaaaaaa\nline3");

        try (RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r")) {
            assertEquals(5, ReverseLineReader.snapOnNewline(randomAccessFile, 5));
        }
    }

    @Test
    public void snapOnNewline_inMiddleLastLine_returnNegative1() throws IOException {
        File file = createFile("snapOnNewLineC", "line1\nline2aaaaaaaaa\nline3");

        try (RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r")) {
            assertEquals(-1, ReverseLineReader.snapOnNewline(randomAccessFile, 2));
        }
    }

    private static File createFile(String fileName, String content) throws IOException {
        File file = new File(tempDir, fileName);
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content);
        }
        return file;
    }
}
