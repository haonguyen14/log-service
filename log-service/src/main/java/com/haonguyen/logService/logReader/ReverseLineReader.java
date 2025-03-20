package com.haonguyen.logService.logReader;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ReverseLineReader {
    public static Optional<String> readLine(RandomAccessFile file, long ptr) throws IOException {
        if (ptr < 0) return Optional.empty();
        StringBuilder sb = new StringBuilder();

        while (ptr >= 0) {
            file.seek(ptr--);
            char c = (char) file.read();

            if (c == '\n')
                break;

            sb.append(c);
        }

        return Optional.of(sb.reverse().toString());
    }

    public static Lines readNLines(RandomAccessFile file, long ptr, int n) throws IOException {
        List<String> lines = new ArrayList<>();

        while (ptr >= 0) {
            Optional<String> line = readLine(file, ptr);

            ptr--; // line break
            if (line.isPresent()) {
                lines.add(line.get());
                ptr -= line.get().length();
            }
            if (lines.size() == n)
                break;
        }

        return Lines.builder()
                .lines(lines)
                .ptr(ptr)
                .build();
    }

    public static Lines readAllLines(RandomAccessFile file, long start, long end) throws IOException {
        byte[] buffer = new byte[(int)(start-end)];
        file.seek(end+1);
        file.read(buffer);

        String str = new String(buffer);
        String[] lines = str.split("\n");
        return Lines.builder().lines(Arrays.asList(lines)).ptr(end).build();
    }

    public static long snapOnNewline(RandomAccessFile file, long ptr) throws IOException {
        while (ptr >= 0) {
            file.seek(ptr);
            if ((char) file.read() == '\n') {
                break;
            }
            ptr--;
        }
        return ptr;
    }
}
