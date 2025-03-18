package com.haonguyen.logService.logReader;

import com.haonguyen.logService.exception.InvalidFilePathException;
import com.haonguyen.logService.json.LogResponse;
import com.haonguyen.logService.logReader.rule.LogRule;
import lombok.Builder;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class LogReader {
    private final LogReaderConfig config;

    public LogReader(@Autowired LogReaderConfig config) {
        this.config = config;
    }

    public LogResponse readLogs(Path filePath, int take, List<LogRule> rules)
            throws ExecutionException, InterruptedException {
        if (!Files.exists(filePath) || !Files.isRegularFile(filePath))
            throw new InvalidFilePathException("Invalid File");

        BlockingQueue<Chunk> chunkQueue = new LinkedBlockingQueue<>(config.getQueueSize());
        Thread chunker = createFileChunkerThread(filePath, config.getChunkSize(), chunkQueue);

        List<WorkUnit> output = new ArrayList<>();
        ExecutorService executorService = Executors.newFixedThreadPool(config.getNumThreads());
        List<Future<Void>> chunkProcessors = createChunkProcessorThreads(
                filePath,
                executorService,
                chunkQueue,
                output,
                rules);

        chunker.start();
        for (Future<Void> future : chunkProcessors) {
           future.get();
        }
        executorService.shutdown();

        Collection<String> lines = output
                .stream()
                .sorted((a, b) -> Long.compare(a.chunk.getPtrStart(), b.chunk.getPtrStart()) * -1)
                .map(WorkUnit::getLines)
                .flatMap(Collection::stream)
                .limit(take)
                .collect(Collectors.toList());

        return LogResponse.builder().logs(lines).build();
    }

    private Thread createFileChunkerThread(Path filePath, int chunkSize, BlockingQueue<Chunk> chunkQueue) {
        return new Thread(() -> {
            try (RandomAccessFile file = new RandomAccessFile(filePath.toFile(), "r")) {
                long ptr = file.length()-1;
                while (ptr >= 0) {
                    long end = Math.max(ptr-chunkSize, 0);
                    end = ReverseLineReader.snapOnNewline(file, end);
                    chunkQueue.put(Chunk.builder().ptrStart(ptr).ptrEnd(end).build());
                    ptr = end - 1;
                }
                for (int i = 0; i < config.getNumThreads(); i++)
                    chunkQueue.put(Chunk.builder().isEOF(true).build());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private List<Future<Void>> createChunkProcessorThreads(
            Path filePath,
            ExecutorService executorService,
            BlockingQueue<Chunk> chunkQueue,
            Collection<WorkUnit> output,
            List<LogRule> rules) {
        return IntStream.range(0, config.getNumThreads())
                .mapToObj(i -> (Future<Void>) executorService.submit(() -> {
                    try (RandomAccessFile file = new RandomAccessFile(filePath.toFile(), "r")) {
                        while (true) {
                            try {
                                Chunk chunk = chunkQueue.take();
                                if (chunk.isEOF()) {
                                    break;
                                }

                                List<String> lines = new ArrayList<>();
                                long ptr = chunk.getPtrStart();
                                while (ptr > chunk.getPtrEnd()) {
                                    Lines ls = ReverseLineReader.readNLines(file, ptr, config.getBatchSize());
                                    lines.addAll(ls.getLines()
                                            .stream()
                                            .filter(l -> rules.stream().allMatch(r -> r.isMatched(l)))
                                            .collect(Collectors.toList()));
                                    ptr = ls.getPtr();
                                }

                                output.add(WorkUnit.builder().lines(lines).chunk(chunk).build());
                            } catch (InterruptedException e) {
                                break;
                            }
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }))
                .collect(Collectors.toList());
    }

    @Builder
    private static class WorkUnit {
        @Getter
        private Collection<String> lines;

        @Getter
        private Chunk chunk;
    }

    @Builder
    private static class Chunk {
        @Getter
        private long ptrStart;

        @Getter
        private long ptrEnd;

        @Getter
        @Builder.Default
        private boolean isEOF = false;
    }
}
