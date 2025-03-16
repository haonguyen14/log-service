package com.haonguyen.logService.logReader.rule;

public interface LogRule {
    boolean isMatched(String log);
}
