package com.haonguyen.logService.logReader;

import com.haonguyen.logService.exception.InvalidFilePathException;
import com.haonguyen.logService.json.LogResponse;
import com.haonguyen.logService.logReader.rule.LogRule;
import lombok.Builder;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class LogReader {
    private final LogReaderConfig config;

    public LogReader(@Autowired LogReaderConfig config) {
        this.config = config;
    }

    public LogResponse readLogs(Path filePath, Optional<Long> ptr, int take, List<LogRule> rules) {
        if (!Files.exists(filePath) || !Files.isRegularFile(filePath))
            throw new InvalidFilePathException("Invalid File");

        BlockingQueue<Chunk> chunkQueue = new LinkedBlockingQueue<>(config.getQueueSize());
        createFileChunkerThread(filePath, ptr, config.getChunkSize(), chunkQueue).start();

        ConcurrentMap<Long, WorkUnit> checkoutLine = new ConcurrentHashMap<>();
        ExecutorService executorService = Executors.newFixedThreadPool(config.getNumThreads());
        AtomicBoolean shouldStop = new AtomicBoolean(false);
        List<Future<Void>> chunkProcessors = createChunkProcessorThreads(
                filePath,
                executorService,
                shouldStop,
                chunkQueue,
                checkoutLine,
                rules);


        long verifyingChunkId = 0;
        long nextChunkPtr = -1;
        Collection<String> output = new ArrayList<>();
        while (output.size() <= take) {
            if (checkoutLine.containsKey(verifyingChunkId) && checkoutLine.get(verifyingChunkId) != null) {
                WorkUnit work = checkoutLine.get(verifyingChunkId);
                output.addAll(work.getLines());
                nextChunkPtr = work.chunk.getPtrEnd()-1;

                verifyingChunkId++;
            }
            if (chunkProcessors.stream().allMatch(Future::isDone)) break;
        }

        shouldStop.set(true);
        executorService.shutdown();

        return LogResponse.builder()
                .logs(output)
                .nextPtr(nextChunkPtr)
                .build();
    }

    private Thread createFileChunkerThread(Path filePath, Optional<Long> startPtr, int chunkSize, BlockingQueue<Chunk> chunkQueue) {
        return new Thread(() -> {
            try (RandomAccessFile file = new RandomAccessFile(filePath.toFile(), "r")) {
                long ptr = (startPtr.isEmpty() || startPtr.get() >= file.length() || startPtr.get() <= 0) ?
                        file.length() - 1 :
                        startPtr.get();
                long id = 0;
                while (ptr >= 0) {
                    long end = Math.max(ptr - chunkSize, 0);
                    end = ReverseLineReader.snapOnNewline(file, end);
                    chunkQueue.put(Chunk.builder().id(id++).ptrStart(ptr).ptrEnd(end).build());
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
            AtomicBoolean shouldStop,
            BlockingQueue<Chunk> chunkQueue,
            ConcurrentMap<Long, WorkUnit> checkoutLine,
            List<LogRule> rules) {
        return IntStream.range(0, config.getNumThreads())
                .mapToObj(i -> (Future<Void>) executorService.submit(() -> {
                    try (RandomAccessFile file = new RandomAccessFile(filePath.toFile(), "r")) {
                        while (!shouldStop.get()) {
                            try {
                                Chunk chunk = chunkQueue.take();
                                if (chunk.isEOF()) {
                                    break;
                                }
                                WorkUnit work = WorkUnit.builder()
                                        .lines(ReverseLineReader.readAllLines(
                                                file,
                                                chunk.getPtrStart(),
                                                chunk.getPtrEnd()).getLines())
                                        .chunk(chunk).build();
                                checkoutLine.put(chunk.getId(), work);
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
        private long id;

        @Getter
        private long ptrStart;

        @Getter
        private long ptrEnd;

        @Getter
        @Builder.Default
        private boolean isEOF = false;
    }
}
