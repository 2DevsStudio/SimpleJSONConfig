package com.twodevsstudio.simplejsonconfig.exceptions;

import com.google.gson.JsonElement;
import lombok.Getter;

import java.util.List;

@Getter
public class ConfigDeprecatedException extends RuntimeException {
    private final List<String> missingFields;
    private final List<String> redundantFields;
    private final JsonElement sourceJson;
    
    public ConfigDeprecatedException(String configName, List<String> missingFields, List<String> redundantFields,
                                     JsonElement sourceJson) {
        
        super(String.format("%nYour config \"%s\" is outdated!%n\tMissing fields: %s%n\tRedundant fields: %s",
                configName, missingFields, redundantFields
        ));
        
        this.missingFields = missingFields;
        this.redundantFields = redundantFields;
        this.sourceJson = sourceJson;
    }
    
}
