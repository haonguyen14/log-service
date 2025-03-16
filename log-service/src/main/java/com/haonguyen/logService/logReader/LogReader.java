package com.haonguyen.logService.logReader;

import com.haonguyen.logService.exception.InvalidFilePathException;
import com.haonguyen.logService.json.LogResponse;
import com.haonguyen.logService.logReader.rule.LogRule;
import com.haonguyen.logService.logReader.rule.LogTextContainRule;
import lombok.Builder;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
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
        BlockingQueue<WorkUnit> workQueue = new LinkedBlockingQueue<>(config.getQueueSize());
        Thread lineReaderThread = createLineReaderThread(filePath, take, workQueue);

        ExecutorService executorService = Executors.newFixedThreadPool(config.getNumThreads());
        List<WorkUnit> output = new ArrayList<>();
        List<Future<Void>> lineProcessorThreads = createLineProcessorThreads(executorService, workQueue, output, rules);

        lineReaderThread.start();
        for (Future<Void> processor : lineProcessorThreads) {
            processor.get();
        }
        executorService.shutdown();

        Collection<String> lines = output
                .stream()
                .sorted(Comparator.comparingInt(WorkUnit::getOrder))
                .map(WorkUnit::getLines)
                .flatMap(Collection::stream)
                .limit(take)
                .collect(Collectors.toList());

        return LogResponse.builder().logs(lines).build();
    }

    private Thread createLineReaderThread(Path filePath, int take, BlockingQueue<WorkUnit> workQueue) {
        if (!Files.exists(filePath) || !Files.isRegularFile(filePath))
            throw new InvalidFilePathException("Invalid File");

        return new Thread(() -> {
            try (RandomAccessFile file = new RandomAccessFile(filePath.toFile(), "r")) {
                long ptr = file.length() - 1;
                int order = 0;
                int totalLines = 0;

                while (ptr >= 0 && totalLines <= take) {
                    Lines lines = ReverseLineReader.readNLines(file, ptr, config.getBatchSize());
                    workQueue.put(WorkUnit.builder()
                            .order(order++)
                            .lines(lines.getLines())
                            .isEOF(false)
                            .build());

                    ptr = lines.getPtr();
                    totalLines += lines.getLines().size();
                }

                for (int i = 0; i < config.getNumThreads(); i++)
                    workQueue.put(WorkUnit.builder().isEOF(true).build());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private List<Future<Void>> createLineProcessorThreads(
            ExecutorService executorService,
            BlockingQueue<WorkUnit> workQueue,
            Collection<WorkUnit> output,
            List<LogRule> rules) {
        return IntStream.range(0, config.getNumThreads())
                .mapToObj(i -> (Future<Void>) executorService.submit(() -> {
                    while (true) {
                        try {
                            WorkUnit work = workQueue.take();

                            if (work.isEOF()) {
                                break;
                            }

                            output.add(WorkUnit
                                    .builder()
                                    .lines(work.getLines()
                                            .stream()
                                            .filter((s) -> rules.stream().allMatch(r -> r.isMatched(s)))
                                            .collect(Collectors.toList()))
                                    .order(work.getOrder())
                                    .build());
                        } catch (InterruptedException e) {
                            break;
                        }
                    }
                }))
                .collect(Collectors.toList());
    }

    @Builder
    private static class WorkUnit {
        @Getter
        private Collection<String> lines;

        @Getter
        private int order;

        @Getter
        @Builder.Default
        private boolean isEOF = false;
    }
}
