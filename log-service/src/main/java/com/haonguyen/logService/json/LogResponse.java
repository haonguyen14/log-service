package com.haonguyen.logService.json;

import lombok.Builder;
import lombok.Getter;

import java.util.Collection;

@Builder
public class LogResponse {
    @Getter
    Collection<String> logs;

    @Getter
    long nextPtr;
}
