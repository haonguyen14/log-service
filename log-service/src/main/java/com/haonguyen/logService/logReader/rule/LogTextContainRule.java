package com.haonguyen.logService.logReader.rule;

public class LogTextContainRule implements LogRule {
    private final String txt;

    public LogTextContainRule(String contains) {
        this.txt = contains.toLowerCase();
    }

    @Override
    public boolean isMatched(String log) {
        return log.toLowerCase().contains(txt);
    }
}
