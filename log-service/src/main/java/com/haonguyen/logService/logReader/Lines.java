package com.haonguyen.logService.logReader;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;

@Builder
public class Lines {
    @Getter
    private Collection<String> lines;

    @Getter
    private long ptr;
}
