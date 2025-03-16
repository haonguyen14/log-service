package com.haonguyen.logService.logReader.rule;

public class LogTextIsNotEmptyRule implements LogRule {
    @Override
    public boolean isMatched(String log) {
        return !(log.isEmpty() && log.isBlank());
    }
}
