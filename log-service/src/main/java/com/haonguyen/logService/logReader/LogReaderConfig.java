package com.haonguyen.logService.logReader;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LogReaderConfig {
    @Value("${logReader.queueSize}")
    @Getter
    private int queueSize;

    @Value("${logReader.batchSize}")
    @Getter
    private int batchSize;

    @Value("${logReader.numThreads}")
    @Getter
    private int numThreads;
}
