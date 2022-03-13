package com.twodevsstudio.simplejsonconfig;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class GlobalCacheConfiguration {
    
    private long entryLifespanSeconds = 5L * 60L;
    private long scanIntervalSeconds = 2L * 60L;
    private int maxSize = 100;
}
